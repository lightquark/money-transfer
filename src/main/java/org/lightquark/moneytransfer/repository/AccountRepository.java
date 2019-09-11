package org.lightquark.moneytransfer.repository;

import org.lightquark.moneytransfer.model.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountRepository {

    private static final AccountRepository INSTANCE = new AccountRepository();

    public static AccountRepository getInstance() {
        return INSTANCE;
    }

    private Map<Long, Account> store = new ConcurrentHashMap<>();

    public Account find(Long id) {
        return store.get(id);
    }

    public List<Account> findAll() {
        return new ArrayList<>(store.values());
    }

    public void save(Account account) {
        store.putIfAbsent(account.getId(), account);
    }

    public boolean delete(Long id) {
        if (store.get(id) == null) {
            return false;
        }
        store.remove(id);
        return true;
    }

    public void clear() {
        store.clear();
    }
}
