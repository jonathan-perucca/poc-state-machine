package com.github.jonathanperucca.model;

public class Exchange {
    String uuid;
    States currentState;

    public Exchange(String uuid) {
        this.uuid = uuid;
        this.currentState = States.PENDING;
    }

    public String getUuid() {
        return uuid;
    }

    public States getCurrentState() {
        return currentState;
    }

    public void setCurrentState(States currentState) {
        this.currentState = currentState;
    }

    @Override
    public String toString() {
        return "Exchange{" +
                "uuid='" + uuid + '\'' +
                ", currentState=" + currentState +
                '}';
    }
}