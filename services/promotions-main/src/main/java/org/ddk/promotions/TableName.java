package org.ddk.promotions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that maps a bean class to a DynamoDB table name.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableName {
    /**
     * Returns the name of the DynamoDB table backing the annotated entity as a string.
     * 
     * @return Name of the DynamoDB table backing the annotated entity.
     */
    String value();
}
