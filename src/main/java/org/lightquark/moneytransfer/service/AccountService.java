package org.lightquark.moneytransfer.service;

import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.repository.AccountRepository;

import java.util.List;

@Slf4j
public class AccountService {

    private static final AccountService INSTANCE = new AccountService();

    public static AccountService getInstance() {
        return INSTANCE;
    }

    private AccountRepository accountRepository = AccountRepository.getInstance();

    public Account find(Long id) {
        return accountRepository.find(id);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account create() {
        Account account = new Account();
        accountRepository.save(account);
        return account;
    }

    public boolean delete(Long id) {
        return accountRepository.delete(id);
    }

    public void clear() {
        accountRepository.clear();
    }
}
