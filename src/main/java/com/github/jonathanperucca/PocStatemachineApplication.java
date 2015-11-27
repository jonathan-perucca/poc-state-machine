package com.github.jonathanperucca;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.model.States;
import com.github.jonathanperucca.service.AnotherService;
import com.github.jonathanperucca.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;

import static com.github.jonathanperucca.model.Events.*;

@SpringBootApplication
public class PocStatemachineApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PocStatemachineApplication.class, args);
    }

    @Autowired
    AnotherService anotherService;

    @Autowired
    ExchangeService exchangeService;

    @Override
    public void run(String... args) throws Exception {
        exchangeService.showDB();

        String uuid = "xch-prd-1";
        String uuid2 = "xch-prd-2";
        Exchange exchange = exchangeService.getExchange(uuid);

        Message<Events> message = MessageBuilder
                .withPayload(Events.ACCEPT)
                .setHeaderIfAbsent("exchange", exchange)
                .build();

        exchangeService.handleEvent(message);

        exchangeService.showDB();

        anotherService.doRequestStateMachine();

        message = MessageBuilder
                .withPayload(Events.EXCHANGE_STARTED)
                .setHeaderIfAbsent("exchange", exchange)
                .build();

        exchangeService.handleEvent(message);

        exchangeService.showDB();

        anotherService.doRequestStateMachine();

        // Now - Use second exchange update against stateMachine

        // PENDING
        Exchange exchange2 = exchangeService.getExchange(uuid2);
        message = MessageBuilder
                .withPayload(ACCEPT)
                .setHeaderIfAbsent("exchange", exchange2)
                .build();

        // PENDING -> READY
        boolean eventAccepted = exchangeService.handleEvent(message);

        System.out.println("Event accepted : " + eventAccepted);

        exchangeService.showDB();

        anotherService.doRequestStateMachine();

        // ACCEPT when exchange is READY should not be accepted

        message = MessageBuilder
                .withPayload(ACCEPT)
                .setHeaderIfAbsent("exchange", exchange2)
                .build();

        eventAccepted = exchangeService.handleEvent(message);

        System.out.println("Event accepted : " + eventAccepted);


        // Guard is set on transition (READY -> IN_PROGRESS)
        // it checks if strange is true, then throw BusinessStateMachineException
        exchange2.setStrange(true);

        message = MessageBuilder
                .withPayload(EXCHANGE_STARTED)
                .setHeaderIfAbsent("exchange", exchange2)
                .build();

        exchangeService.handleEvent(message);
    }

}
