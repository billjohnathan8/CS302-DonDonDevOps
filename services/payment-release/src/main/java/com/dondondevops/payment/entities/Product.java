package com.dondondevops.payment.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.dondondevops.payment.TableName;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@Value
@Builder(setterPrefix = "set") // Required Prefix for DynamoDbImmutable to find mutators
@TableName("Product")
@DynamoDbImmutable(builder = Product.ProductBuilder.class)
public class Product {
    @Getter(onMethod_=@DynamoDbPartitionKey)
    UUID productID;
    String Name;
    String Category;
    String Brand;
    int Stock;
    double PriceInSGD;
    LocalDateTime expiryDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}