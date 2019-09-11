package org.lightquark.moneytransfer.controller;

import org.junit.Assert;
import org.junit.Test;
import org.lightquark.moneytransfer.model.Account;

import javax.ws.rs.core.Response;

public class AccountControllerTest extends BaseControllerTest {

    @Test
    public void shouldFindAccount() {

        Account account = accountService.create();
        Assert.assertNotNull(account);

        Response response = assertOkResponse(get("account/" + account.getId().toString()));

        Account returned = response.readEntity(Account.class);
        Assert.assertNotNull(returned);
        Assert.assertEquals(account, returned);
    }

    @Test
    public void shouldNotFindAccount_whenIdIsInvalid() {

        Account account = accountService.create();
        Assert.assertNotNull(account);

        Long invalidId = 1 + account.getId();

        assertBadRequestResponse(get("account/" + invalidId.toString()));
    }

    @Test
    public void shouldFindAllAccounts() {

        for (int i = 0; i < 10; i++) {
            accountService.create();
        }

        Response response = assertOkResponse(get("account/all"));

        Account[] accounts = response.readEntity(Account[].class);
        Assert.assertNotNull(accounts);
        Assert.assertEquals(10, accounts.length);
    }

    @Test
    public void shouldCreateAccount() {

        Response response = assertOkResponse(put("account/create"));

        Account created = response.readEntity(Account.class);
        Assert.assertNotNull(created);
    }

    @Test
    public void shouldDeleteAccount() {

        Response response = assertOkResponse(put("account/create"));

        Account created = response.readEntity(Account.class);
        Assert.assertNotNull(created);

        assertOkResponse(delete("account/" + created.getId().toString()));
    }

    @Test
    public void shouldNotDeleteAccount_whenIdIsInvalid() {

        Response response = assertOkResponse(put("account/create"));

        Account created = response.readEntity(Account.class);
        Assert.assertNotNull(created);

        Long invalidId = 1 + created.getId();

        assertBadRequestResponse(delete("account/" + invalidId.toString()));
    }
}
