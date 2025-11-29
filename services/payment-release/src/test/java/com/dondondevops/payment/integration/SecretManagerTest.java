package com.dondondevops.payment.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.dondondevops.payment.SecretManager;

@SpringBootTest(
    properties = "spring.main.allow-bean-definition-overriding=true"
)
@EnabledIfSystemProperty(named = "spring.profiles.active", matches = "it")
@Import(DynamoDbConfiguration.class)
public class SecretManagerTest {
    
    @Autowired
    private SecretManager secretManager;

    @Test
    public void givesEnvironmentVariable() {
        String result = secretManager.getStripeApiKey();
        assertNotNull(result);
    }
}
