package org.ddk.promotions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.ddk.promotions.TableName;

/**
 * Represents a promotion definition stored in DynamoDB, including validity window
 * and discount rate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("Promotion")
@DynamoDbBean
public class Promotion {
    private UUID id = Objects.requireNonNull(UUID.randomUUID());
    private String name;
    private Instant startTime;
    private Instant endTime;
    private double discountRate; // 0..1

    /**
     * Constructor for Promotion Data Object.
     *
     * @param name Promotion name.
     * @param startTime Promotion start timestamp.
     * @param endTime Promotion end timestamp.
     * @param discountRate Promotion discount rate in range [0, 1].
     */
    public Promotion(String name, Instant startTime, Instant endTime, double discountRate) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.discountRate = discountRate;
    }

    /**
     * Returns the ID of the promotion.
     * 
     * @return The ID of the promotion as a UUID.
     */
    @DynamoDbPartitionKey
    public UUID getId() {
        return id;
    }

    /**
     * Sets the ID of the promotion.  
     * @param id The ID of the promotion as a UUID.
     */
    public void setId(UUID id) {
        this.id = id; 
    }

    /**
     * Returns the name of the promotion.
     * 
     * @return The name of the promotion as a String.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the promotion.
     * 
     * @param name The new name of the promotion.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the Starting Time of the promotion.
     * 
     * @return The Starting Time of the promotion.
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Sets the Starting Time of the promotion.
     * 
     * @param startTime The new Starting Time of the promotion.
     */
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the Ending Time of the promotion.
     * 
     * @return The Ending Time of the promotion.
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Sets the Ending Time of the promotion.
     * 
     * @param endTime The new Ending Time of the promotion.
     */
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Returns the discount rate applied to a product under a specific promotion.
     * 
     * @return The discount rate as a Double.
     */
    public double getDiscountRate() {
        return discountRate;
    }

    /**
     * Sets the discount rate applied to a product under a specific promotion.
     * 
     * @param discountRate The new discount rate.
     */
    public void setDiscountRate(double discountRate) {
        this.discountRate = discountRate;
    }

    /**
     * Indicates whether the promotion is active for the provided instant.
     *
     * @param time Instant to test against the promotion window.
     * @return {@code true} when the instant is between start and end time (inclusive).
     */
    public boolean isActiveAt(Instant time) {
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}
