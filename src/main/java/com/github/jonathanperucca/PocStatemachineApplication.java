package com.github.jonathanperucca;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

import static com.github.jonathanperucca.PocStatemachineApplication.Events.*;
import static com.github.jonathanperucca.PocStatemachineApplication.States.*;

@SpringBootApplication
public class PocStatemachineApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(PocStatemachineApplication.class, args);
    }

    @Autowired
    StateMachine<State, Events> stateMachine;

    @Override
    public void run(String... args) throws Exception {
        stateMachine.sendEvent(ACCEPT);
        stateMachine.sendEvent(EXCHANGE_STARTED);
        stateMachine.sendEvent(EXCHANGE_ENDED);
    }


    @Configuration
    @EnableStateMachine
    static class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

        @Override
        public void configure(StateMachineConfigurationConfigurer<States, Events> config)
                throws Exception {
            config
                    .withConfiguration()
                    .autoStartup(true)
                    .listener(listener());
        }

        @Bean
        public StateMachineListener<States, Events> listener() {
            return new StateMachineListenerAdapter<States, Events>() {
                @Override
                public void stateChanged(State<States, Events> from, State<States, Events> to) {
                    System.out.println("State change to " + to.getId());
                }
            };
        }

        @Override
        public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
            states.withStates()
                    .initial(PENDING)
                    .end(WAITING_END)
                    .states(EnumSet.allOf(States.class));
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
            transitions.withExternal()
                       .source(PENDING).target(READY).event(ACCEPT)
                       .and().withExternal()
                       .source(PENDING).target(CANCEL).event(OWNER_REFUSAL)
                       .and().withExternal()
                       .source(PENDING).target(CANCEL).event(RECEIVER_CANCELLATION)
                       .and().withExternal()
                       .source(PENDING).target(CANCEL).event(SYSTEM_CANCELLATION)
                       .and().withExternal()
                       .source(READY).target(IN_PROGRESS).event(EXCHANGE_STARTED)
                       .and().withExternal()
                       .source(IN_PROGRESS).target(WAITING_END).event(EXCHANGE_ENDED);
        }
    }

    public static enum States {
        PENDING, READY, CANCEL, IN_PROGRESS, WAITING_END
    }

    public static enum Events {
        ACCEPT, EXCHANGE_STARTED, EXCHANGE_ENDED,
        OWNER_REFUSAL, OWNER_CANCELLATION, RECEIVER_CANCELLATION, SYSTEM_CANCELLATION,
    }
}
