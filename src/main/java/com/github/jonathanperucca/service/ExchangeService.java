package com.github.jonathanperucca.service;

import com.github.jonathanperucca.exception.BusinessStateMachineException;
import com.github.jonathanperucca.handler.PersistStateChangeListener;
import com.github.jonathanperucca.handler.PersistStateMachineHandler;
import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.model.States;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ExchangeService {

    private final PersistStateMachineHandler handler;
    private final String exchangeHeader = "exchange";
    private final MapDB mapDB;

    @Autowired
    public ExchangeService(PersistStateMachineHandler handler, MapDB mapDB) {
        this.handler = handler;
        this.mapDB = mapDB;
        this.handler.addPersistStateChangeListener(new MemPersistStateChangeListener());
    }

    public Exchange getExchange(String uuid) {
        return mapDB.getExchange(uuid);
    }

    /**
     * handleEvent
     */

    @Getter @Setter @Builder
    static class HandleEventRequest {
        private Message<Events> message;
        private Exchange exchange;
    }

    public boolean handleEvent(Message<Events> message) {
        HandleEventRequest request = HandleEventRequest.builder().message(message).build();

        return extractExchange()
                .andThen(this::delegateMessageToHandler)
                .apply(request);
    }

    private Function<HandleEventRequest, HandleEventRequest> extractExchange() {
        return (request) -> {
            Exchange exchange = request.getMessage().getHeaders().get(exchangeHeader, Exchange.class);

            request.setExchange(exchange);
            return request;
        };
    }

    private Boolean delegateMessageToHandler(HandleEventRequest request) {
        try {
            return handler.handleEventWithState(request.getMessage(), request.getExchange().getCurrentState());
        } catch (BusinessStateMachineException e) {
            return false;
        }
    }

    /**
     * end handleEvent
     */


    private class MemPersistStateChangeListener implements PersistStateChangeListener {

        @Override
        public void onPersist(State<States,Events> state, Message<Events> message,
                              Transition<States,Events> transition, StateMachine<States,Events> stateMachine) {
            if (message != null && message.getHeaders().containsKey(exchangeHeader)) {
                Exchange exchange = message.getHeaders().get(exchangeHeader, Exchange.class);

                exchange.setCurrentState(state.getId());
                mapDB.putExchange(exchange.getUuid(), exchange);
            }
        }

        @Override
        public void onError(Exception exception) {
            throw new BusinessStateMachineException("StateMachine added a business exception", exception);
        }
    }
}