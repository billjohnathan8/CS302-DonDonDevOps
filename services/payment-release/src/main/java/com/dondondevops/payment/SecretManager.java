package com.dondondevops.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SecretManager {
    @Autowired
    private Environment env;

    public String getStripeApiKey() {
        return env.getProperty("stripe.api-key");
    }

    public String getStripeWebhookSecret() {
        return env.getProperty("stripe.webhook-secret");
    }
}
