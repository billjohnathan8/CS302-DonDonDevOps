package com.dondondevops.inventory.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import io.micronaut.context.annotation.Requires;
import io.micronaut.rabbitmq.connect.ChannelInitializer;
import jakarta.inject.Singleton;

/**
 * RabbitMQ configuration to ensure exchanges are created at startup.
 */
@Singleton
@Requires(beans = Channel.class)
public class RabbitMQConfig extends ChannelInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Override
    public void initialize(Channel channel, String name) throws IOException {
        // Declare inventory.events exchange (topic, durable)
        channel.exchangeDeclare(
            "inventory.events",
            BuiltinExchangeType.TOPIC,
            true
        );

        LOG.info("RabbitMQ exchange 'inventory.events' declared successfully");
    }
}
