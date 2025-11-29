package com.dondondevops.inventory.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class InstantZConverter implements AttributeConverter<Instant> {

    @Override
    public AttributeValue transformFrom(Instant input) {
        return AttributeValue.builder().s(input.truncatedTo(ChronoUnit.SECONDS).toString()).build(); // always ends with 'Z'
    }

    @Override
    public Instant transformTo(AttributeValue attributeValue) {
        return Instant.parse(attributeValue.s()).truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }

    @Override
    public EnhancedType<Instant> type() {
        return EnhancedType.of(Instant.class);
    }
}
