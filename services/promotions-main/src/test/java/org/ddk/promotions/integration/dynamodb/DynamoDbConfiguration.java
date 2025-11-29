package org.ddk.promotions.integration.dynamodb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@Profile("it")
public class DynamoDbConfiguration {
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    public DynamoDbContainer dynamoDb() {
        return new DynamoDbContainer();
    }
    
    @Bean
    public DynamoDbClient dynamoDbClient(DynamoDbContainer dynamoDb) {
        return DynamoDbClient.builder()
                .endpointOverride(dynamoDb.getURI())
                .region(Region.US_EAST_1)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")))
                .build();
    }
    
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}

