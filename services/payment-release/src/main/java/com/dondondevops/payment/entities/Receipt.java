package com.dondondevops.payment.entities;

import java.util.UUID;

import com.dondondevops.payment.TableName;

import lombok.Builder;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Value
@Builder(setterPrefix = "set")
@TableName("Receipt")
@DynamoDbImmutable(builder = Receipt.ReceiptBuilder.class)
public class Receipt {
    private final UUID id;
    private final String chargeId;
    private final String paymentIntentId;
    
    @DynamoDbPartitionKey
    public UUID getId() {
        return id;
    }
}
