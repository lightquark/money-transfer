package org.lightquark.moneytransfer.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lightquark.moneytransfer.model.Account;

import java.util.List;

public class AccountServiceTest {

    private AccountService accountService = AccountService.getInstance();

    @BeforeClass
    public static void beforeClass() {
    }

    @Before
    public void before() {
        accountService.clear();
    }

    @Test
    public void shouldCreateAccount() {

        Account account = accountService.create();
        Assert.assertNotNull(account);
    }

    @Test
    public void shouldFindAccount() {

        Account account = accountService.create();
        Assert.assertNotNull(account);

        Account accountFromRepository = accountService.find(account.getId());
        Assert.assertEquals(account, accountFromRepository);
    }

    @Test
    public void shouldFindAllAccounts() {

        Account[] accounts = new Account[10];
        for (int j = 0; j < accounts.length; j++) {
            accounts[j] = accountService.create();
            Assert.assertNotNull(accounts[j]);

        }

        List<Account> accountsFromRepository = accountService.findAll();
        Assert.assertEquals(accounts.length, accountsFromRepository.size());

        for (int j = 0; j < accounts.length; j++) {
            Assert.assertTrue(accountsFromRepository.contains(accounts[j]));
        }
    }

    @Test
    public void shouldDeleteAccount() {

        Account account = accountService.create();
        Assert.assertNotNull(account);

        Account accountFromRepository = accountService.find(account.getId());
        Assert.assertEquals(account, accountFromRepository);

        boolean result = accountService.delete(account.getId());
        Assert.assertTrue(result);

        Account deletedAccount = accountService.find(account.getId());
        Assert.assertNull(deletedAccount);
    }

}
