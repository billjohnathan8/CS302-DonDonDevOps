package com.dondondevops.inventory.exceptionHandler;

import java.util.Map;

import com.dondondevops.inventory.exception.UUIDNotFoundException;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.server.exceptions.NotFoundException;

@Controller
public class CustomNotFoundHandler {

    @Error(global = true)
    public HttpResponse<?> handleException(HttpRequest<?> request, RuntimeException e) {
        return HttpResponse.serverError()
            .body(Map.of(
                "error", "Server error",
                "message", e.getMessage()
            ));
    }
    
    @Error(global = true)
    public HttpResponse<?> handleNotFound(HttpRequest<?> request, UUIDNotFoundException e) {
        return HttpResponse.notFound()
            .body(Map.of(
                "error", "Not Found",
                "message", "ID: " + e.getId().toString() + " could not be found."
            ));
    }


    @Error(global = true)
    public HttpResponse<Map<String, String>> handleNotFound(HttpRequest<?> request, NotFoundException e) {
        return HttpResponse.notFound()
            .body(Map.of(
                "error", "Not found",
                "message", request.getUri().toString() + " does not exist."
            ));
    }
}
