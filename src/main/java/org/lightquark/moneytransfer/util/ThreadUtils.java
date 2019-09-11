package org.lightquark.moneytransfer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ThreadUtils {

    public static void runThread(Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.start();
    }

    public static String getThreadInfo() {
        Thread thread = Thread.currentThread();
        return String.format("%s %s", thread.getId(), thread.getName());
    }

    public static void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("InterruptedException occurs.", e);
        }
    }
}
