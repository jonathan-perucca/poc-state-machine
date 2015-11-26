package com.github.jonathanperucca.model;

public class Exchange {
        String uuid;
        String smId;
        States currentState;

        public Exchange(String uuid, String smId, States state) {
            this.uuid = uuid;
            this.smId = smId;
            this.currentState = state;
        }

        public String getUuid() {
            return uuid;
        }

        public String getSmId() {
            return smId;
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
                    ", smId='" + smId + '\'' +
                    ", currentState=" + currentState +
                    '}';
        }
    }