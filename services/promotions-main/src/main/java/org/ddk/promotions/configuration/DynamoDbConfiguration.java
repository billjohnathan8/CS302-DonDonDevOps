package org.ddk.promotions.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Spring configuration component that creates the DynamoDB low-level and enhanced
 * clients used by the repositories.
 */
@Configuration
@Profile("!it")
public class DynamoDbConfiguration {

    /**
     * Creates a new configuration instance. Spring instantiates this class via reflection.
     */
    public DynamoDbConfiguration() {
        // Default constructor required by Spring.
    }

    /**
     * Builds the base {@link DynamoDbClient} configured with the application properties.
     *
     * @param properties dynamo-specific configuration such as region and endpoint.
     * @return Configured DynamoDB client.
     */
    @Bean
    public DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
        var builder = DynamoDbClient.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (properties.getEndpoint() != null) {
            builder = builder.endpointOverride(properties.getEndpoint());
        }
        return builder.build();
    }

    /**
     * Exposes an {@link DynamoDbEnhancedClient} backed by the base client.
     *
     * @param dynamoDbClient Base client instance.
     * @return Enhanced client offering high-level table operations.
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
