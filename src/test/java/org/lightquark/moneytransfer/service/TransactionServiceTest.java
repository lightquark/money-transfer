package org.lightquark.moneytransfer.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.repository.TransactionRepository;
import org.lightquark.moneytransfer.util.ThreadUtils;

import java.math.BigDecimal;

@Slf4j
public class TransactionServiceTest {

    private static final int REPEAT_COUNT = 500;
    private static final BigDecimal AMOUNT_BIG = BigDecimal.valueOf(1_000_000_000L);

    private TransactionService transactionService = TransactionService.getInstance();
    private TransactionRepository transactionRepository = TransactionRepository.getInstance();
    private AccountService accountService = AccountService.getInstance();

    @BeforeClass
    public static void beforeClass() {
        new TransactionProcessor().start();
    }

    @Test
    public void concurrentModifications_whenDeposit_forSingleAccount() {

        Account account = accountService.create();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 1; i < REPEAT_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(i);
            transactionService.deposit(account, amount);
            total = total.add(amount);
        }

        waitUntilTransactionsAreCompleted();
        Assert.assertEquals(total, account.getBalance());
    }

    @Test
    public void concurrentModifications_whenDeposit_forMultipleAccounts() {

        Account[] accounts = createAccounts();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 1; i < REPEAT_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(i);
            for (int j = 0; j < accounts.length; j++) {
                transactionService.deposit(accounts[j], amount);
            }
            total = total.add(amount);
        }

        waitUntilTransactionsAreCompleted();
        assertAccounts(total, accounts);
    }

    @Test
    public void concurrentModifications_whenWithdraw_forSingleAccount() {

        Account account = accountService.create();
        BigDecimal total = AMOUNT_BIG;
        transactionService.deposit(account, total);

        for (int i = 1; i < REPEAT_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(i);
            transactionService.withdraw(account, amount);
            total = total.subtract(amount);
        }

        waitUntilTransactionsAreCompleted();
        Assert.assertEquals(total, account.getBalance());
    }

    @Test
    public void concurrentModifications_whenWithdraw_forMultipleAccounts() {

        Account[] accounts = createAccounts();
        BigDecimal total = AMOUNT_BIG;
        for (int j = 0; j < accounts.length; j++) {
            transactionService.deposit(accounts[j], total);
        }

        for (int i = 1; i < REPEAT_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(i);
            for (int j = 0; j < accounts.length; j++) {
                transactionService.withdraw(accounts[j], amount);
            }
            total = total.subtract(amount);
        }

        waitUntilTransactionsAreCompleted();
        assertAccounts(total, accounts);
    }

    @Test
    public void concurrentModifications_whenDepositAndWithdraw_forSingleAccount() {

        Account account = accountService.create();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = REPEAT_COUNT; i > 0; i--) {
            BigDecimal amount = BigDecimal.valueOf(i);
            if (i % 2 == 0) {
                transactionService.deposit(account, amount);
                total = total.add(amount);
            } else {
                transactionService.withdraw(account, amount);
                total = total.subtract(amount);
            }
        }

        waitUntilTransactionsAreCompleted();
        Assert.assertEquals(total, account.getBalance());
    }

    @Test
    public void concurrentModifications_whenDepositAndWithdraw_forMultipleAccounts() {

        Account[] accounts = createAccounts();
        BigDecimal total = BigDecimal.ZERO;

        for (int i = REPEAT_COUNT; i > 0; i--) {
            BigDecimal amount = BigDecimal.valueOf(i);
            if (i % 2 == 0) {
                for (int j = 0; j < accounts.length; j++) {
                    transactionService.deposit(accounts[j], amount);
                }
                total = total.add(amount);
            } else {
                for (int j = 0; j < accounts.length; j++) {
                    transactionService.withdraw(accounts[j], amount);
                }
                total = total.subtract(amount);
            }
        }

        waitUntilTransactionsAreCompleted();
        assertAccounts(total, accounts);
    }

    @Test
    public void concurrentModifications_whenTransfer_forTwoAccounts() {

        Account sourceAccount = accountService.create();
        Account destinationAccount = accountService.create();
        BigDecimal sourceAccountBalance = AMOUNT_BIG;
        BigDecimal destinationAccountBalance = BigDecimal.ZERO;
        transactionService.deposit(sourceAccount, sourceAccountBalance);

        for (int i = 1; i < REPEAT_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(i);
            transactionService.transfer(sourceAccount, destinationAccount, amount);
            sourceAccountBalance = sourceAccountBalance.subtract(amount);
            destinationAccountBalance = destinationAccountBalance.add(amount);
        }

        waitUntilTransactionsAreCompleted();
        Assert.assertEquals(sourceAccountBalance, sourceAccount.getBalance());
        Assert.assertEquals(destinationAccountBalance, destinationAccount.getBalance());
    }

    @Test
    public void concurrentModifications_whenTransfer_forMultipleAccounts() {

        Account[] sourceAccounts = createAccounts();
        Account[] destinationAccounts = createAccounts();

        BigDecimal sourceAccountBalance = AMOUNT_BIG;
        BigDecimal destinationAccountBalance = BigDecimal.ZERO;
        for (int j = 0; j < sourceAccounts.length; j++) {
            transactionService.deposit(sourceAccounts[j], sourceAccountBalance);
        }

        for (int i = 1; i < REPEAT_COUNT; i++) {
            BigDecimal amount = BigDecimal.valueOf(i);
            for (int j = 0; j < sourceAccounts.length; j++) {
                transactionService.transfer(sourceAccounts[j], destinationAccounts[j], amount);
            }
            sourceAccountBalance = sourceAccountBalance.subtract(amount);
            destinationAccountBalance = destinationAccountBalance.add(amount);
        }

        waitUntilTransactionsAreCompleted();
        assertAccounts(sourceAccountBalance, sourceAccounts);
        assertAccounts(destinationAccountBalance, destinationAccounts);
    }

    private void waitUntilTransactionsAreCompleted() {
        int transactionCount;
        do {
            transactionCount = transactionRepository.getSize();
            log.debug("Left {} transactions", transactionCount);
            if (transactionCount > 0) {
                ThreadUtils.sleep(100L);
            }
        } while (transactionCount > 0);
    }

    private void assertAccounts(BigDecimal expected, Account[] accounts) {
        for (int j = 0; j < accounts.length; j++) {
            Assert.assertEquals(expected, accounts[j].getBalance());
        }
    }

    private Account[] createAccounts() {
        Account[] accounts = new Account[10];
        for (int j = 0; j < accounts.length; j++) {
            accounts[j] = accountService.create();
        }
        return accounts;
    }
}
