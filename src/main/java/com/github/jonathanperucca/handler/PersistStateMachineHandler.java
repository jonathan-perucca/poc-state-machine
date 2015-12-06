package com.github.jonathanperucca.handler;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import com.github.jonathanperucca.exception.BusinessStateMachineException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * {@code PersistStateMachineHandler} is a recipe which can be used to
 * handle a state change of an arbitrary entity in a persistent storage.
 *
 */
public class PersistStateMachineHandler extends LifecycleObjectSupport {

	private final StateMachine<States, Events> stateMachine;
	private final PersistingStateChangeInterceptor interceptor = new PersistingStateChangeInterceptor();
	private final CompositePersistStateChangeListener listeners = new CompositePersistStateChangeListener();

	public PersistStateMachineHandler(StateMachine<States, Events> stateMachine) {
		Assert.notNull(stateMachine, "State machine must be set");
		this.stateMachine = stateMachine;
	}

	@Override
	protected void onInit() throws Exception {
		stateMachine.getStateMachineAccessor().doWithAllRegions(function -> function.addStateMachineInterceptor(interceptor));
	}

	/**
	 * HandleEventWithState
	 */

	@Getter @Setter @Builder
	static class HandleEventWithStateRequest {
		private Message<Events> message;
		private States state;
	}

	public boolean handleEventWithState(Message<Events> message, States state) {
		HandleEventWithStateRequest request = HandleEventWithStateRequest.builder().message(message).state(state).build();

		return reinitializeTo()
				.andThen(this::sendEventToStateMachine)
				.apply(request);
	}

	private Function<HandleEventWithStateRequest, HandleEventWithStateRequest> reinitializeTo() {
		return (request) -> {
			stateMachine.stop();
			List<StateMachineAccess<States, Events>> withAllRegions = stateMachine.getStateMachineAccessor()
																				  .withAllRegions();
			for (StateMachineAccess<States, Events> a : withAllRegions) {
				a.resetStateMachine(new DefaultStateMachineContext<>(request.getState(), null, null, null));
			}
			resetStateMachineErrors();
			stateMachine.start();
			return request;
		};
	}

	private Boolean sendEventToStateMachine(HandleEventWithStateRequest request) {
		boolean eventAccepted = stateMachine.sendEvent(request.getMessage());

		if(!eventAccepted) {
			throw new BusinessStateMachineException("Event not accepted by state machine");
		}
		return true;
	}

	/**
	 * end HandleEventWithState
	 */

	private void resetStateMachineErrors() {
		stateMachine.setStateMachineError(null);
	}

	public void addPersistStateChangeListener(PersistStateChangeListener listener) {
		listeners.register(listener);
	}


	private class PersistingStateChangeInterceptor extends StateMachineInterceptorAdapter<States,Events> {

		@Override
		public void postStateChange(State<States, Events> state, Message<Events> message, Transition<States, Events> transition, StateMachine<States, Events> stateMachine) {
			listeners.onPersist(state, message, transition, stateMachine);
		}

		@Override
		public Exception stateMachineError(StateMachine<States, Events> stateMachine, Exception exception) {
			listeners.onError(exception);
			return exception;
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

		@Override
		public void onError(Exception exception) {
			for (Iterator<PersistStateChangeListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				PersistStateChangeListener listener = iterator.next();
				listener.onError(exception);
			}
		}
	}

}