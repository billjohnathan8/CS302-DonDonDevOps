package com.dondondevops.inventory.config;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Requires(env = { "prod" })
@Factory
public class ProdConfig {

    @Singleton
    public DynamoDbClient createClient() {
        return DynamoDbClient
                .builder()
                .credentialsProvider(ContainerCredentialsProvider.create())
                .region(Region.AP_SOUTHEAST_1)
                .build();
    }

    @Inject
    @Singleton
    public DynamoDbEnhancedClient createEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }
}
