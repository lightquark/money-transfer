package org.lightquark.moneytransfer.service;

import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.config.Config;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.model.Transaction;
import org.lightquark.moneytransfer.model.TransactionStatus;
import org.lightquark.moneytransfer.repository.TransactionRepository;
import org.lightquark.moneytransfer.util.ThreadUtils;
import org.lightquark.moneytransfer.util.TransactionUtils;

import java.math.BigDecimal;

@Slf4j
public class TransactionProcessor {

    private static final long WAIT_TIMEOUT = 1000;
    private static final String THREAD_COUNT_PROPERTY = "transaction_processing_threads";
    private static final int DEFAULT_THREAD_COUNT = 1;

    private TransactionRepository transactionRepository = TransactionRepository.getInstance();
    private AccountService accountService = AccountService.getInstance();

    public void start() {
        Integer threadCount = Config.getInteger(THREAD_COUNT_PROPERTY, DEFAULT_THREAD_COUNT);
        log.info("Started processing of transactions. Thread's count {}", threadCount);

        for (int i = 0; i < threadCount; i++) {
            ThreadUtils.runThread(getRunnable());
        }
    }

    private Runnable getRunnable() {
        return () -> {
            // This process will work until the application is stopped
            while (true) {
                Transaction transaction = transactionRepository.getNext();
                if (transaction == null) {
                    log.debug("Thread is waiting for transactions {}", ThreadUtils.getThreadInfo());
                    ThreadUtils.sleep(WAIT_TIMEOUT);
                } else {
                    processTransaction(transaction);
                }
            }
        };
    }

    private void processTransaction(Transaction transaction) {
        log.info("Process transaction {}", transaction);
        if (transaction.getStatus() != TransactionStatus.UNPROCESSED) {
            log.info("Skipping transaction because it is already processed {}", transaction);
            return;
        }

        switch (transaction.getType()) {
            case DEPOSIT:
                deposit(transaction);
                break;
            case WITHDRAW:
                withdraw(transaction);
                break;
            case TRANSFER:
                transfer(transaction);
                break;
        }
    }

    private void deposit(Transaction transaction) {
        if (transaction.getSourceAccountId() == null
                || transaction.getAmount() == null || BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0) {
            log.info("Invalid transaction params {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        Account account = accountService.find(transaction.getSourceAccountId());
        if (account == null) {
            log.info("Invalid account id. Transaction {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        TransactionUtils.doWithLock(account, transaction, this::processDepositTransaction);
    }

    private void processDepositTransaction(Account account, Transaction transaction) {
        if (havePrecedingTransactions(account, transaction)) {
            return;
        }

        if (transaction.getAmount() == null || BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0) {
            log.info("Invalid amount. Transaction {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        account.setBalance(account.getBalance().add(transaction.getAmount()));
        account.setLastTransactionId(transaction.getId());
        transaction.setStatus(TransactionStatus.COMPLETED);
        log.info("Transaction {} completed successfully", transaction);
    }

    private void withdraw(Transaction transaction) {
        if (transaction.getSourceAccountId() == null
                || transaction.getAmount() == null || BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0) {
            log.info("Invalid transaction params {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        Account account = accountService.find(transaction.getSourceAccountId());
        if (account == null) {
            log.info("Invalid account id. Transaction {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        TransactionUtils.doWithLock(account, transaction, this::processWithdrawTransaction);
    }

    private void processWithdrawTransaction(Account account, Transaction transaction) {
        if (havePrecedingTransactions(account, transaction)) {
            return;
        }

        if (transaction.getAmount() == null || BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0
                || account.getBalance().compareTo(transaction.getAmount()) < 0) {
            log.info("Invalid amount. Transaction {}, account balance {}", transaction, account.getBalance());
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        account.setLastTransactionId(transaction.getId());
        transaction.setStatus(TransactionStatus.COMPLETED);
        log.info("Transaction {} completed successfully", transaction);
    }

    private void transfer(Transaction transaction) {
        if (transaction.getSourceAccountId() == null || transaction.getDestinationAccountId() == null
                || transaction.getSourceAccountId().equals(transaction.getDestinationAccountId())
                || transaction.getAmount() == null || BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0) {
            log.info("Invalid transaction params {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        Account sourceAccount = accountService.find(transaction.getSourceAccountId());
        if (sourceAccount == null) {
            log.info("Invalid source account id. Transaction {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }
        // For simplicity, I do not consider the case when sourceAccount is deleted before this line (due to concurrency issues)
        Account destinationAccount = accountService.find(transaction.getDestinationAccountId());
        if (destinationAccount == null) {
            log.info("Invalid destination account id. Transaction {}", transaction);
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        TransactionUtils.doWithLock(sourceAccount, destinationAccount, transaction, this::processTransferTransaction);
    }

    private void processTransferTransaction(Account sourceAccount, Account destinationAccount,
            Transaction transaction) {
        if (havePrecedingTransactions(sourceAccount, transaction)) {
            return;
        }

        if (transaction.getAmount() == null || BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0
                || sourceAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            log.info("Invalid amount. Transaction {}, source account balance {}", transaction,
                    sourceAccount.getBalance());
            transaction.setStatus(TransactionStatus.INVALID);
            return;
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transaction.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(transaction.getAmount()));
        sourceAccount.setLastTransactionId(transaction.getId());
        transaction.setStatus(TransactionStatus.COMPLETED);
        log.info("Transaction {} completed successfully", transaction);
    }

    /**
     * Let's imagine that we two transactions: A and B.
     * Sometimes, due to concurrency issues, we may start processing B before A. This is incorrect,
     * so we must return B into the storage and release the thread for another transactions.
     * @return true, if the transaction should be processed later
     */
    private boolean havePrecedingTransactions(Account account, Transaction transaction) {
        if (account.getLastTransactionId() + 1 != transaction.getId()) {
            log.debug("Firstly, we need to process the previous transactions. Transaction {}, Account {}, thread {}",
                    transaction, account, ThreadUtils.getThreadInfo());
            transactionRepository.pushBack(transaction);
            return true;
        }
        return false;
    }

}
