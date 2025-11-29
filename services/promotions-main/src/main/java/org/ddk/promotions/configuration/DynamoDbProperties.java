package org.ddk.promotions.configuration;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed configuration holder for DynamoDB settings.
 */
@Component
@ConfigurationProperties(prefix = "dynamodb")
public class DynamoDbProperties {

    /**
     * Creates a new instance with default region and no endpoint override.
     */
    public DynamoDbProperties() {
        // Default constructor for configuration binding.
    }

    /**
     * AWS region where the target DynamoDB table resides.
     */
    private String region = "ap-southeast-1";

    /**
     * Optional endpoint override (useful for local DynamoDB).
     */
    private String endpoint;

    /**
     * Returns the AWS region where the DynamoDB table resides.
     *
     * @return Region identifier (for example {@code ap-southeast-1}).
     */
    public String getRegion() {
        return region;
    }

    /**
     * Updates the AWS region to target.
     *
     * @param region Region identifier accepted by AWS clients.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Builds an endpoint override URI when configured.
     *
     * @return Endpoint URI or {@code null} when not provided.
     */
    public URI getEndpoint() {
        if (endpoint == null || endpoint.isBlank()) {
            return null;
        }
        return URI.create(endpoint);
    }

    /**
     * Sets the raw endpoint string (commonly used for local DynamoDB instances).
     *
     * @param endpoint Endpoint string, e.g. {@code http://localhost:8000}.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
