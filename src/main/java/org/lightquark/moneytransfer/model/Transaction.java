package org.lightquark.moneytransfer.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "type", "status", "sourceAccountId", "destinationAccountId", "amount"})
public class Transaction {

    private final Long id;
    private final TransactionType type;
    @Setter
    private TransactionStatus status;
    private final Long sourceAccountId;
    private final Long destinationAccountId;
    private final BigDecimal amount;

    @JsonCreator
    public Transaction(@JsonProperty("id") Long id,
            @JsonProperty("type") TransactionType type,
            @JsonProperty("status") TransactionStatus status,
            @JsonProperty("sourceAccountId") Long sourceAccountId,
            @JsonProperty("destinationAccountId") Long destinationAccountId,
            @JsonProperty("amount") BigDecimal amount) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }
}
