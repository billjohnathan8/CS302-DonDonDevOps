package com.dondondevops.inventory.config;

import java.net.URI;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Requires(env = { "dev" })
@Requires(bean = IDynamoDBConfig.class)
@Factory
public class DevConfig {

    @Inject
    @Singleton
    public DynamoDbClient createClient(IDynamoDBConfig config) {
        return DynamoDbClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("localstack", "localstack")))
                .region(Region.AP_SOUTHEAST_1)
                .endpointOverride(URI.create("http://" + config.getDynamodbHost() + ":" + config.getDynamodbPort()))
                .build();
    }

    @Inject
    @Singleton
    public DynamoDbEnhancedClient createEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }
}
