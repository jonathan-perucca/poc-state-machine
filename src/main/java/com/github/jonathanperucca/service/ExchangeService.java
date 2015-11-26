package com.github.jonathanperucca.service;

import com.github.jonathanperucca.handler.PersistStateMachineHandler;
import com.github.jonathanperucca.handler.PersistStateMachineHandler.PersistStateChangeListener;
import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.model.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.processor.StateMachineHandler;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.github.jonathanperucca.model.States.READY;

@Service
public class ExchangeService {

    private final PersistStateMachineHandler handler;
    private final PersistStateChangeListener listener = new MemPersistStateChangeListener();
    private final String exchangeHeader = "exchange";

    private Map<String, Exchange> exchangeDB;

    @Autowired
    public ExchangeService(PersistStateMachineHandler handler) {
        this.handler = handler;
        this.handler.addPersistStateChangeListener(listener);

        String uuid = "xch-prd-1";
        String uuid2 = "xch-prd-2";
        exchangeDB = new HashMap<>(2);
        exchangeDB.put(uuid, new Exchange(uuid));
        exchangeDB.put(uuid2, new Exchange(uuid2));
    }

    public void onEnterAccept() {
        System.out.println("ExchangeService : set status = " + READY);
    }

    public void onExitAccept() {
        System.out.println("ExchangeService : leaving status " + READY);
    }

    public void handleEvent(Message<Events> message) {
        Exchange exchange = message.getHeaders().get(exchangeHeader, Exchange.class);
        handler.handleEventWithState(message, exchange.getCurrentState());
    }

    public void showDB() {
        exchangeDB.forEach((id, exchange) -> System.out.println("exchange (" + id + ") has state : " + exchange.getCurrentState()));
    }

    public Exchange getExchange(String uuid) {
        return exchangeDB.get(uuid);
    }

    private class MemPersistStateChangeListener implements PersistStateChangeListener {

        @Override
        public void onPersist(State<States,Events> state, Message<Events> message,
                              Transition<States,Events> transition, StateMachine<States,Events> stateMachine) {
            if (message != null && message.getHeaders().containsKey(exchangeHeader)) {
                Exchange exchange = message.getHeaders().get(exchangeHeader, Exchange.class);

                exchange.setCurrentState(state.getId());
                exchangeDB.put(exchange.getUuid(), exchange);
            }
        }
    }
}