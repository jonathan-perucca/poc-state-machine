package com.github.jonathanperucca;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.model.States;
import com.github.jonathanperucca.service.AnotherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.statemachine.StateMachine;

import static com.github.jonathanperucca.model.Events.*;

@SpringBootApplication
public class PocStatemachineApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PocStatemachineApplication.class, args);
    }

    @Autowired
    StateMachine<States, Events> stateMachine;

    @Autowired
    AnotherService anotherService;

    @Autowired
    ApplicationContext context;

    @Override
    public void run(String... args) throws Exception {
        Exchange exchange = context.getBean(Exchange.class);
        System.out.println(exchange);

        anotherService.doRequestStateMachine();

        stateMachine.sendEvent(ACCEPT);

        anotherService.doRequestStateMachine();

        stateMachine.sendEvent(EXCHANGE_STARTED);

        anotherService.doRequestStateMachine();

        stateMachine.sendEvent(EXCHANGE_ENDED);
    }

}
