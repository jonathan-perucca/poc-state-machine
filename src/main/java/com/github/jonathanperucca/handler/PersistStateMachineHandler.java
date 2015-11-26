package com.github.jonathanperucca.handler;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;

/**
 * {@code PersistStateMachineHandler} is a recipe which can be used to
 * handle a state change of an arbitrary entity in a persistent storage.
 *
 * @author Janne Valkealahti
 *
 */
public class PersistStateMachineHandler extends LifecycleObjectSupport {

	private final StateMachine<States, Events> stateMachine;
	private final PersistingStateChangeInterceptor interceptor = new PersistingStateChangeInterceptor();
	private final CompositePersistStateChangeListener listeners = new CompositePersistStateChangeListener();

	/**
	 * Instantiates a new persist state machine handler.
	 *
	 * @param stateMachine the state machine
	 */
	public PersistStateMachineHandler(StateMachine<States, Events> stateMachine) {
		Assert.notNull(stateMachine, "State machine must be set");
		this.stateMachine = stateMachine;
	}

	@Override
	protected void onInit() throws Exception {
		stateMachine.getStateMachineAccessor().doWithAllRegions(function -> function.addStateMachineInterceptor(interceptor));
	}

	/**
	 * Handle event with entity.
	 *
	 * @param event the event
	 * @param state the state
	 * @return true if event was accepted
	 */
	public boolean handleEventWithState(Message<Events> event, States state) {
		stateMachine.stop();
		List<StateMachineAccess<States, Events>> withAllRegions = stateMachine.getStateMachineAccessor().withAllRegions();
		for (StateMachineAccess<States, Events> a : withAllRegions) {
			a.resetStateMachine(new DefaultStateMachineContext<>(state, null, null, null));
		}
		stateMachine.start();
		return stateMachine.sendEvent(event);
	}

	/**
	 * Adds the persist state change listener.
	 *
	 * @param listener the listener
	 */
	public void addPersistStateChangeListener(PersistStateChangeListener listener) {
		listeners.register(listener);
	}

	/**
	 * The listener interface for receiving persistStateChange events.
	 * The class that is interested in processing a persistStateChange
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPersistStateChangeListener</code> method. When
	 * the persistStateChange event occurs, that object's appropriate
	 * method is invoked.
	 */
	public interface PersistStateChangeListener {

		/**
		 * Called when state needs to be persisted.
		 *
		 * @param state the state
		 * @param message the message
		 * @param transition the transition
		 * @param stateMachine the state machine
		 */
		void onPersist(State<States,Events> state, Message<Events> message, Transition<States,Events> transition,
				StateMachine<States,Events> stateMachine);
	}

	private class PersistingStateChangeInterceptor extends StateMachineInterceptorAdapter<States,Events> {

		@Override
		public void postStateChange(State<States, Events> state, Message<Events> message, Transition<States, Events> transition, StateMachine<States, Events> stateMachine) {
			listeners.onPersist(state, message, transition, stateMachine);
		}
	}

	private class CompositePersistStateChangeListener extends AbstractCompositeListener<PersistStateChangeListener> implements
		PersistStateChangeListener {

		@Override
		public void onPersist(State<States, Events> state, Message<Events> message,
				Transition<States, Events> transition, StateMachine<States, Events> stateMachine) {
			for (Iterator<PersistStateChangeListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				PersistStateChangeListener listener = iterator.next();
				listener.onPersist(state, message, transition, stateMachine);
			}
		}
	}

}