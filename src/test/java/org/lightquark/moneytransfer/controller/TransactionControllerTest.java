package org.lightquark.moneytransfer.controller;

import org.junit.Assert;
import org.junit.Test;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.model.Transaction;
import org.lightquark.moneytransfer.model.TransactionType;

import java.math.BigDecimal;

import javax.ws.rs.core.Response;

public class TransactionControllerTest extends BaseControllerTest {

    private static final String TRANSACTION_DEPOSIT_URI = "transaction/deposit";
    private static final String TRANSACTION_WITHDRAW_URI = "transaction/withdraw";
    private static final String TRANSACTION_TRANSFER_URI = "transaction/transfer";

    private static final BigDecimal AMOUNT_BIG = BigDecimal.valueOf(1_000_000_000L);
    private static final BigDecimal AMOUNT_POSITIVE = BigDecimal.valueOf(10);
    private static final BigDecimal AMOUNT_ZERO = BigDecimal.ZERO;
    private static final BigDecimal AMOUNT_NEGATIVE = BigDecimal.valueOf(-10);

    /////////////////////////////////////////////////////////////////
    // Deposit transactions
    /////////////////////////////////////////////////////////////////

    @Test
    public void shouldCreateDepositTransaction() {

        Account account = accountService.create();
        Assert.assertNotNull(account);

        Response response = assertOkResponse(put(TRANSACTION_DEPOSIT_URI,
                "accountId", account.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));

        Transaction returned = response.readEntity(Transaction.class);
        Assert.assertNotNull(returned);
        Assert.assertEquals(account.getId(), returned.getSourceAccountId());
        Assert.assertEquals(TransactionType.DEPOSIT, returned.getType());
        Assert.assertEquals(AMOUNT_POSITIVE, returned.getAmount());
    }

    @Test
    public void shouldNotCreateDepositTransaction_whenMissedParams() {
        Account account = accountService.create();
        Assert.assertNotNull(account);

        assertBadRequestResponse(put(TRANSACTION_DEPOSIT_URI,
                "accountId", account.getId().toString()));

        assertBadRequestResponse(put(TRANSACTION_DEPOSIT_URI,
                "amount", AMOUNT_POSITIVE.toString()));

        assertBadRequestResponse(put(TRANSACTION_DEPOSIT_URI));
    }

    @Test
    public void shouldNotCreateDepositTransaction_whenInvalidAccountId() {
        Account account = accountService.create();
        Assert.assertNotNull(account);

        Long invalidId = 1 + account.getId();

        assertBadRequestResponse(put(TRANSACTION_DEPOSIT_URI,
                "accountId", invalidId.toString(),
                "amount", AMOUNT_POSITIVE.toString()));
    }

    @Test
    public void shouldNotCreateDepositTransaction_whenZeroAmount() {
        Account account = accountService.create();
        Assert.assertNotNull(account);

        assertBadRequestResponse(put(TRANSACTION_DEPOSIT_URI,
                "accountId", account.getId().toString(),
                "amount", AMOUNT_ZERO.toString()));
    }

    @Test
    public void shouldNotCreateDepositTransaction_whenNegativeAmount() {
        Account account = accountService.create();
        Assert.assertNotNull(account);

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", account.getId().toString(),
                "amount", AMOUNT_NEGATIVE.toString()));
    }

    /////////////////////////////////////////////////////////////////
    // Withdraw transactions
    /////////////////////////////////////////////////////////////////

    @Test
    public void shouldCreateWithdrawTransaction() {

        Account account = accountService.create();
        Assert.assertNotNull(account);
        account.setBalance(AMOUNT_BIG);

        Response response = assertOkResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", account.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));

        Transaction returned = response.readEntity(Transaction.class);
        Assert.assertNotNull(returned);
        Assert.assertEquals(account.getId(), returned.getSourceAccountId());
        Assert.assertEquals(TransactionType.WITHDRAW, returned.getType());
        Assert.assertEquals(AMOUNT_POSITIVE, returned.getAmount());
    }

    @Test
    public void shouldNotCreateWithdrawTransaction_whenMissedParams() {
        Account account = accountService.create();
        Assert.assertNotNull(account);
        account.setBalance(AMOUNT_BIG);

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", account.getId().toString()));

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "amount", AMOUNT_POSITIVE.toString()));

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI));
    }

    @Test
    public void shouldNotCreateWithdrawTransaction_whenInvalidAccountId() {
        Account account = accountService.create();
        Assert.assertNotNull(account);
        account.setBalance(AMOUNT_BIG);

        Long invalidId = 1 + account.getId();

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", invalidId.toString(),
                "amount", AMOUNT_POSITIVE.toString()));
    }

    @Test
    public void shouldNotCreateWithdrawTransaction_whenZeroAmount() {
        Account account = accountService.create();
        Assert.assertNotNull(account);
        account.setBalance(AMOUNT_BIG);

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", account.getId().toString(),
                "amount", AMOUNT_ZERO.toString()));
    }

    @Test
    public void shouldNotCreateWithdrawTransaction_whenNegativeAmount() {
        Account account = accountService.create();
        Assert.assertNotNull(account);
        account.setBalance(AMOUNT_BIG);

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", account.getId().toString(),
                "amount", AMOUNT_NEGATIVE.toString()));
    }

    @Test
    public void shouldNotCreateWithdrawTransaction_whenNotEnoughFunds() {
        Account account = accountService.create();
        Assert.assertNotNull(account);
        account.setBalance(BigDecimal.valueOf(100));

        assertBadRequestResponse(put(TRANSACTION_WITHDRAW_URI,
                "accountId", account.getId().toString(),
                "amount", BigDecimal.valueOf(101).toString()));
    }

    /////////////////////////////////////////////////////////////////
    // Transfer transactions
    /////////////////////////////////////////////////////////////////

    @Test
    public void shouldCreateTransferTransaction() {

        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(AMOUNT_BIG);
        Account destinationAccount = accountService.create();
        Assert.assertNotNull(destinationAccount);

        Response response = assertOkResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", destinationAccount.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));

        Transaction returned = response.readEntity(Transaction.class);
        Assert.assertNotNull(returned);
        Assert.assertEquals(sourceAccount.getId(), returned.getSourceAccountId());
        Assert.assertEquals(destinationAccount.getId(), returned.getDestinationAccountId());
        Assert.assertEquals(TransactionType.TRANSFER, returned.getType());
        Assert.assertEquals(AMOUNT_POSITIVE, returned.getAmount());
    }

    @Test
    public void shouldNotCreateTransferTransaction_whenMissedParams() {
        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(AMOUNT_BIG);
        Account destinationAccount = accountService.create();
        Assert.assertNotNull(destinationAccount);

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "destinationAccountId", destinationAccount.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", destinationAccount.getId().toString()));

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI));
    }

    @Test
    public void shouldNotCreateTransferTransaction_whenInvalidAccountId() {
        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(AMOUNT_BIG);
        Account destinationAccount = accountService.create();
        Assert.assertNotNull(destinationAccount);

        Long invalidId = 1 + destinationAccount.getId();

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", invalidId.toString(),
                "destinationAccountId", destinationAccount.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", invalidId.toString(),
                "amount", AMOUNT_POSITIVE.toString()));
    }

    @Test
    public void shouldNotCreateTransferTransaction_whenZeroAmount() {
        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(AMOUNT_BIG);
        Account destinationAccount = accountService.create();
        Assert.assertNotNull(destinationAccount);

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", destinationAccount.getId().toString(),
                "amount", AMOUNT_ZERO.toString()));
    }

    @Test
    public void shouldNotCreateTransferTransaction_whenNegativeAmount() {
        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(AMOUNT_BIG);
        Account destinationAccount = accountService.create();
        Assert.assertNotNull(destinationAccount);

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", destinationAccount.getId().toString(),
                "amount", AMOUNT_NEGATIVE.toString()));
    }

    @Test
    public void shouldNotCreateTransferTransaction_whenNotEnoughFunds() {
        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(BigDecimal.valueOf(100));
        Account destinationAccount = accountService.create();
        Assert.assertNotNull(destinationAccount);

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", destinationAccount.getId().toString(),
                "amount", BigDecimal.valueOf(101).toString()));
    }

    @Test
    public void shouldNotCreateTransferTransaction_whenDestinationIsEqualToSource() {

        Account sourceAccount = accountService.create();
        Assert.assertNotNull(sourceAccount);
        sourceAccount.setBalance(AMOUNT_BIG);

        assertBadRequestResponse(put(TRANSACTION_TRANSFER_URI,
                "sourceAccountId", sourceAccount.getId().toString(),
                "destinationAccountId", sourceAccount.getId().toString(),
                "amount", AMOUNT_POSITIVE.toString()));
    }

}
