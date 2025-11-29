package com.dondondevops.inventory.config;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Requires(env = { "dev" })
@Singleton
public class DevDynamoConfig implements IDynamoDBConfig {

    @Value("${dynamodb-local.host}")
    private String hostnames;

    @Value("${dynamodb-local.port}")
    private int exposedPorts;

    @Override
    public String getDynamodbHost() {
        return hostnames;
    }

    @Override
    public int getDynamodbPort() {
        return exposedPorts;
    }
}
