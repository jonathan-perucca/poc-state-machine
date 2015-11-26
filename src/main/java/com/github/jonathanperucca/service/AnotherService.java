package com.github.jonathanperucca.service;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Service;

@Service
public class AnotherService {

    @Autowired
    StateMachine<States, Events> stateMachine;

    public void doRequestStateMachine() {
        State<States, Events> state = stateMachine.getState();
        System.out.println("AnotherService : state : " + state);
    }

}