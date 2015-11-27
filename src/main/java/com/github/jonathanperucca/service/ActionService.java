package com.github.jonathanperucca.service;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class ActionService {

    public Action<States, Events> entryAction() {
        return context -> System.out.println(format("Entering action with state %s", context.getStateMachine().getState().getId()));
    }

    public Action<States, Events> exitAction() {
        return context -> System.out.println(format("Exiting action with state %s", context.getStateMachine().getState().getId()));
    }

}
