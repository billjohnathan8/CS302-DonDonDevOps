package com.dondondevops.inventory.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/api/health")
public class HealthController {
    
    @Get()
    public HttpResponse<?> getHealth() {
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            Map<String, String> body = Map.of("status", "up", "ip", ipAddress);
            return HttpResponse.ok(body);

        } catch(UnknownHostException e) {
            Map<String, String> body = Map.of("status", "error", "ip", e.getMessage());
            return HttpResponse.serverError(body);
        }
    }
}
