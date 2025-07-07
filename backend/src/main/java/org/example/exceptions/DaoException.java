// Package declaration
package org.example.exceptions;

/**
 * Custom exception class for handling errors in DAO (Data Access Object) operations.
 * Extends RuntimeException so it can be thrown without being declared in a method's `throws` clause.
 */
public class DaoException extends RuntimeException {

    /**
     * Constructor that creates a DaoException with a specific error message.
     *
     * @param message the detail message describing the cause of the exception.
     */
    public DaoException(String message) {
        // Call the constructor of the superclass (RuntimeException) with the error message
        super(message);
    }
}
