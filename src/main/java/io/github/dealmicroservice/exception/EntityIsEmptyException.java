package io.github.dealmicroservice.exception;

/**
 * Exception для пустых сущностей
 */
public class EntityIsEmptyException extends RuntimeException {

    public EntityIsEmptyException(String message) {
        super(message);
    }

}

