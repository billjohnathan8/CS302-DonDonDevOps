package org.ddk.promotions.configuration;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DynamoDbPropertiesTest {

    @Test
    void defaults_and_endpointParsing_work() {
        var props = new DynamoDbProperties();

        assertEquals("ap-southeast-1", props.getRegion());
        assertNull(props.getEndpoint(), "endpoint should be null when unset");

        props.setEndpoint("   ");
        assertNull(props.getEndpoint(), "blank endpoint strings are ignored");

        props.setEndpoint("http://localhost:8000");
        assertEquals(URI.create("http://localhost:8000"), props.getEndpoint());

        props.setRegion("ap-southeast-2");
        assertEquals("ap-southeast-2", props.getRegion());
    }
}
