package org.ddk.promotions.integration.dynamodb;

import java.net.URI;

import org.springframework.beans.factory.DisposableBean;
import org.testcontainers.containers.GenericContainer;

public class DynamoDbContainer extends GenericContainer<DynamoDbContainer> implements DisposableBean {
    public DynamoDbContainer() {
        super("amazon/dynamodb-local:latest");
        withExposedPorts(8000);
        withCommand("-jar DynamoDBLocal.jar -inMemory -sharedDb");
    }
    
    public URI getURI() {
        return URI.create("http://" + getHost() + ":" + getFirstMappedPort());
    }

    @Override
    public void destroy() throws Exception {
        stop();
    }
}
