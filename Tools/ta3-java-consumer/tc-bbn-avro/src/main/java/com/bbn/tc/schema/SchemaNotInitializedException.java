/*
 * This software delivered to the Government with unlimited rights pursuant to contract FA8750-C-15-7559.
 */

package com.bbn.tc.schema;

/**
 * Exception when the schema is not initialized while we
 *  are trying to serialize/deserialize
 * Created by jkhoury
 */
public class SchemaNotInitializedException extends Exception {

    public SchemaNotInitializedException(String message){
        super(message);
    }

    public SchemaNotInitializedException(String message, Throwable T){
        super(message, T);
    }
}
