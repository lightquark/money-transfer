package org.lightquark.moneytransfer.repository;

import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.model.Transaction;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class TransactionRepository {

    private static final TransactionRepository INSTANCE = new TransactionRepository();

    public static TransactionRepository getInstance() {
        return INSTANCE;
    }

    private Deque<Transaction> store = new ConcurrentLinkedDeque<>();

    public Transaction getNext() {
        return store.pollFirst();
    }

    public void pushBack(Transaction transaction) {
        store.addFirst(transaction);
    }

    public void save(Transaction transaction) {
        store.add(transaction);
    }

    public int getSize() {
        //This is inefficient operation, but I use it only for logging in the tests
        return store.size();
    }
}
