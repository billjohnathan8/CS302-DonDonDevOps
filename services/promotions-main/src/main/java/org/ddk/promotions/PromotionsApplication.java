package org.ddk.promotions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Promotions Spring Boot application.
 */
@SpringBootApplication
public class PromotionsApplication {

    /**
     * Default constructor required by Spring Boot when instantiating the application class.
     */
    public PromotionsApplication() {
        // No state to initialize.
    }

    /**
     * Bootstraps the Spring application context.
     *
     * @param args Command-line arguments forwarded to Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(PromotionsApplication.class, args);
    }
}
