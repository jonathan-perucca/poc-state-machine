package com.github.jonathanperucca.exception;

public class BusinessStateMachineException extends RuntimeException {

    public BusinessStateMachineException(String message) {
        super(message);
    }

    public BusinessStateMachineException(String message, Exception exception) {
        super(message, exception);
    }
}
