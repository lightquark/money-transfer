package org.lightquark.moneytransfer;

import org.lightquark.moneytransfer.service.TransactionProcessor;
import org.lightquark.moneytransfer.web.JettyWebServer;

public class MoneyTransferApplication {

    public static void main(String[] args) {
        new TransactionProcessor().start();
        new JettyWebServer().start();
    }
}
