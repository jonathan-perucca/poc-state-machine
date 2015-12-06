package com.github.jonathanperucca.handler;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public interface PersistStateChangeListener {

	/**
	 * Called when state needs to be persisted.
	 *
	 * @param state the state
	 * @param message the message
	 * @param transition the transition
	 * @param stateMachine the state machine
	 */
	void onPersist(State<States, Events> state, Message<Events> message,
				   Transition<States, Events> transition, StateMachine<States, Events> stateMachine);

	void onError(Exception exception);
}