package com.dondondevops.inventory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;

@MicronautTest
public class HealthEndpointTest {
    
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testGetHealthEndpoint() {
        HttpRequest<?> request = HttpRequest.GET("/api/health").accept(MediaType.APPLICATION_JSON);
        HttpResponse<String> response = client.toBlocking().exchange(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.status());
    }
}
