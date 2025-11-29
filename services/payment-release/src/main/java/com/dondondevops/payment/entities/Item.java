package com.dondondevops.payment.entities;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private UUID id;
    private String name;
    
    public UUID getId() {
        return id;
    }

    public static class Builder {
        private final Item instance;

        public Builder() {
            this(new Item());
        }

        public Builder(Item item) {
            instance = new Item(item.id, item.name);
        }

        public Item build() {
            if (instance.id == null) {
                instance.id = UUID.randomUUID();
            }
            return instance;
        }

        public Builder withId(UUID id) {
            instance.id = id;
            return this;
        }

        public Builder withName(String name) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }
            instance.name = name;
            return this;
        }
    }
}
