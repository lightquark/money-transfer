package org.lightquark.moneytransfer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lightquark.moneytransfer.model.Account;
import org.lightquark.moneytransfer.model.Transaction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionUtils {

    private static final long LOCK_TIMEOUT = 1000L;

    public static void doWithLock(Account account, Transaction transaction, BiConsumer<Account, Transaction> consumer) {
        Lock lock = account.getLock();
        try {
            if (lock.tryLock(LOCK_TIMEOUT, MILLISECONDS)) {
                try {
                    consumer.accept(account, transaction);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("Failed to modify entity {}. Transaction {}", account, transaction, e);
        }
    }

    public static void doWithLock(Account sourceAccount, Account destinationAccount, Transaction transaction,
            TriConsumer<Account, Account, Transaction> consumer) {
        Lock sourceLock = sourceAccount.getLock();
        Lock destinationLock = sourceAccount.getLock();
        try {
            if (sourceLock.tryLock(1000L, MILLISECONDS) && destinationLock.tryLock(1000L, MILLISECONDS)) {
                try {
                    consumer.accept(sourceAccount, destinationAccount, transaction);
                } finally {
                    sourceLock.unlock();
                    destinationLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("Failed to modify entities {} {}. Transaction {}",
                    sourceAccount, destinationAccount, transaction, e);
        }
    }

}
