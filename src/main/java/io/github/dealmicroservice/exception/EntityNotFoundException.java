package io.github.dealmicroservice.exception;

/**
 * Exception отсутствия сущности
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

}

