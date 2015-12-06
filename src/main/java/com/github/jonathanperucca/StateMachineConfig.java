package com.github.jonathanperucca;

import com.github.jonathanperucca.exception.BusinessStateMachineException;
import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.model.States;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import static com.github.jonathanperucca.model.Events.*;
import static com.github.jonathanperucca.model.States.*;
import static java.util.EnumSet.allOf;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

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
                .states(allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
                .withExternal().source(PENDING).target(READY).event(ACCEPT)
                .and()
                .withExternal().source(PENDING).target(CANCEL).event(OWNER_REFUSAL)
                .and()
                .withExternal().source(PENDING).target(CANCEL).event(RECEIVER_CANCELLATION)
                .and()
                .withExternal().source(PENDING).target(CANCEL).event(SYSTEM_CANCELLATION)
                .and()
                .withExternal().guard(exchangeGuard()).source(READY).target(IN_PROGRESS).event(EXCHANGE_STARTED)
                .and()
                .withExternal().source(IN_PROGRESS).target(WAITING_END).event(EXCHANGE_ENDED);
    }

    @Bean
    public Guard<States, Events> exchangeGuard() {
        return context -> {
            Exchange exchange = (Exchange) context.getMessageHeader("exchange");
            if(exchange.isStrange()) {
                context.getStateMachine().setStateMachineError(new BusinessStateMachineException("beurk"));
                return false;
            }
            return true;
        };
    }
}