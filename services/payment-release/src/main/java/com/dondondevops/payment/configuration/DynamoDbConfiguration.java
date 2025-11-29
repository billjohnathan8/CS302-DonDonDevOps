package com.dondondevops.payment.configuration;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Profile("default")
public class DynamoDbConfiguration {
    
    @Bean
    public DynamoDbClient dynamoDbClient(
            @Value("${aws.region:ap-southeast-1}") String awsRegion,
            @Value("${dynamodb.endpoint:}") Optional<String> endpointOverride) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(awsRegion));
        endpointOverride.filter(endpoint -> !endpoint.isBlank())
                .ifPresent(endpoint -> builder.endpointOverride(URI.create(endpoint)));
        return builder.build();
    }
    
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
