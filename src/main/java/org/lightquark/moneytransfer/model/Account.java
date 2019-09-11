package org.lightquark.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "balance", "lastTransactionId", "transactionIdGenerator"})
public class Account {

    private static final AtomicLong ID_GENERATOR = new AtomicLong();

    private final Long id;
    @Setter
    private BigDecimal balance;
    @Setter
    private Long lastTransactionId;
    private AtomicLong transactionIdGenerator;
    @JsonIgnore
    private final transient Lock lock;

    public Account() {
        this.id = ID_GENERATOR.incrementAndGet();
        this.balance = BigDecimal.ZERO;
        this.lastTransactionId = 0L;
        this.lock = new ReentrantLock();
        this.transactionIdGenerator = new AtomicLong();
    }

    @JsonIgnore
    public Long getNextTransactionId() {
        return transactionIdGenerator.incrementAndGet();
    }
}
