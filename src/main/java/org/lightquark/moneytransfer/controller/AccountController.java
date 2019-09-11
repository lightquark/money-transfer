package org.lightquark.moneytransfer.controller;

import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.service.AccountService;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountController {

    private AccountService accountService = AccountService.getInstance();

    @GET
    @Path("/all")
    public Response findAll() {
        List<Account> accounts = accountService.findAll();
        if (accounts == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(accounts).build();
    }

    @GET
    @Path("/{id}")
    public Response find(@PathParam("id") Long id) {
        Account account = accountService.find(id);
        if (account == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(account).build();
    }

    @PUT
    @Path("/create")
    public Response create() {
        Account account = accountService.create();
        log.info("Account {} created successfully.", account);
        return Response.ok(account).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        if (!accountService.delete(id)) {
            log.info("Unable to delete account {}", id);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        log.info("Account {} deleted successfully.", id);
        return Response.ok().build();
    }

}
