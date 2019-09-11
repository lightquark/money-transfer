package org.lightquark.moneytransfer.controller;

import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.model.Transaction;
import org.lightquark.moneytransfer.service.AccountService;
import org.lightquark.moneytransfer.service.TransactionService;

import java.math.BigDecimal;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {

    private TransactionService transactionService = TransactionService.getInstance();
    private AccountService accountService = AccountService.getInstance();

    @PUT
    @Path("/deposit")
    public Response deposit(@QueryParam("accountId") Long accountId, @QueryParam("amount") BigDecimal amount) {
        if (accountId == null || amount == null) {
            log.info("Invalid query params: accountId {}, amount {}", accountId, amount);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Account account = accountService.find(accountId);
        if (account == null) {
            log.info("Invalid accountId {}", accountId);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (BigDecimal.ZERO.compareTo(amount) >= 0) {
            log.info("Invalid amount {}", amount);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Transaction transaction = transactionService.deposit(account, amount);
        log.info("Added transaction {}", transaction);
        return Response.ok(transaction).build();
    }

    @PUT
    @Path("/withdraw")
    public Response withdraw(@QueryParam("accountId") Long accountId, @QueryParam("amount") BigDecimal amount) {
        if (accountId == null || amount == null) {
            log.info("Invalid query params: accountId {}, amount {}", accountId, amount);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Account account = accountService.find(accountId);
        if (account == null) {
            log.info("Invalid accountId {}", accountId);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (BigDecimal.ZERO.compareTo(amount) >= 0 || account.getBalance().compareTo(amount) < 0) {
            log.info("Invalid amount {}", amount);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Transaction transaction = transactionService.withdraw(account, amount);
        log.info("Added transaction {}", transaction);
        return Response.ok(transaction).build();
    }

    @PUT
    @Path("/transfer")
    public Response transfer(@QueryParam("sourceAccountId") Long sourceAccountId,
            @QueryParam("destinationAccountId") Long destinationAccountId,
            @QueryParam("amount") BigDecimal amount) {
        if (sourceAccountId == null || destinationAccountId == null || amount == null
                || sourceAccountId.equals(destinationAccountId)) {
            log.info("Invalid query params: sourceAccountId {}, destinationAccountId {}, amount {}",
                    sourceAccountId, destinationAccountId, amount);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Account sourceAccount = accountService.find(sourceAccountId);
        if (sourceAccount == null) {
            log.info("Invalid sourceAccountId {}", sourceAccountId);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Account destinationAccount = accountService.find(destinationAccountId);
        if (destinationAccount == null) {
            log.info("Invalid destinationAccountId {}", destinationAccountId);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (BigDecimal.ZERO.compareTo(amount) >= 0 || sourceAccount.getBalance().compareTo(amount) < 0) {
            log.info("Invalid amount {}", amount);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        Transaction transaction = transactionService.transfer(sourceAccount, destinationAccount, amount);
        log.info("Added transaction {}", transaction);
        return Response.ok(transaction).build();
    }

}
