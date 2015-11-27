package com.github.jonathanperucca.model;

public class Exchange {
    String uuid;
    States currentState;
    boolean strange;

    public Exchange(String uuid) {
        this.uuid = uuid;
        this.currentState = States.PENDING;
        this.strange = false;
    }

    public boolean isStrange() {
        return strange;
    }

    public void setStrange(boolean strange) {
        this.strange = strange;
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