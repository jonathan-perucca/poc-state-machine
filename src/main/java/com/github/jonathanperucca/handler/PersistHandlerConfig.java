package com.github.jonathanperucca.handler;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import com.github.jonathanperucca.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;

@Configuration
public class PersistHandlerConfig {

    @Autowired
    private StateMachine<States, Events> stateMachine;

    @Bean
    public ExchangeService exchangeService() {
        return new ExchangeService(persistStateMachineHandler());
    }

    @Bean
    public PersistStateMachineHandler persistStateMachineHandler() {
        return new PersistStateMachineHandler(stateMachine);
    }
}
