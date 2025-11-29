package com.dondondevops.inventory.exception;

import java.util.UUID;

public class UUIDNotFoundException extends RuntimeException {

    private UUID id;
    
    public UUIDNotFoundException(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
