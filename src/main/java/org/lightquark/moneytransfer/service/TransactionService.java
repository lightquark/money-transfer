package org.lightquark.moneytransfer.service;

import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.model.Transaction;
import org.lightquark.moneytransfer.model.TransactionStatus;
import org.lightquark.moneytransfer.model.TransactionType;
import org.lightquark.moneytransfer.repository.TransactionRepository;

import java.math.BigDecimal;

@Slf4j
public class TransactionService {

    private static final TransactionService INSTANCE = new TransactionService();

    public static TransactionService getInstance() {
        return INSTANCE;
    }

    private TransactionRepository transactionRepository = TransactionRepository.getInstance();

    public Transaction deposit(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction(account.getNextTransactionId(), TransactionType.DEPOSIT,
                TransactionStatus.UNPROCESSED, account.getId(), null, amount);
        transactionRepository.save(transaction);
        return transaction;
    }

    public Transaction withdraw(Account account, BigDecimal amount) {
        Transaction transaction = new Transaction(account.getNextTransactionId(), TransactionType.WITHDRAW,
                TransactionStatus.UNPROCESSED, account.getId(), null, amount);
        transactionRepository.save(transaction);
        return transaction;
    }

    public Transaction transfer(Account sourceAccount, Account destinationAccount, BigDecimal amount) {
        Transaction transaction = new Transaction(sourceAccount.getNextTransactionId(), TransactionType.TRANSFER,
                TransactionStatus.UNPROCESSED, sourceAccount.getId(), destinationAccount.getId(), amount);
        transactionRepository.save(transaction);
        return transaction;
    }

}
