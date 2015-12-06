package com.github.jonathanperucca;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.model.States;
import com.github.jonathanperucca.service.ExchangeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.github.jonathanperucca.model.Events.ACCEPT;
import static com.github.jonathanperucca.model.Events.EXCHANGE_ENDED;
import static com.github.jonathanperucca.model.States.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationTest.class)
public class PocStatemachineApplicationTests {

	@Autowired
	StateMachine<States, Events> stateMachine;

	@Autowired
	ExchangeService exchangeService;

	@Test
	public void simple_state_transition() throws Exception {
		reinitializeTo(PENDING);

		StateMachineTestPlan<States, Events> testPlan = StateMachineTestPlanBuilder.<States, Events>
				builder()
				.stateMachine(stateMachine)
				.step()
					.expectState(PENDING)
					.and()
				.step()
					.sendEvent(ACCEPT)
					.expectState(READY)
					.and()
				.build();

		testPlan.test();
	}

	@Test
	public void when_final_state_transition_reached_then_statemachine_reinitialize() throws Exception {
		reinitializeTo(IN_PROGRESS);

		StateMachineTestPlan<States, Events> testPlan = StateMachineTestPlanBuilder.<States, Events>
				builder()
			   .stateMachine(stateMachine)
			   .step()
				   .expectState(IN_PROGRESS)
				   .and()
			   .step()
				   .sendEvent(EXCHANGE_ENDED)
					.expectTransition(1)
					.expectStateExited(1)
					.expectStateMachineStopped(1)
				   .and()
			   .build();


		testPlan.test();
	}

	private void reinitializeTo(States state) {
		stateMachine.stop();
		List<StateMachineAccess<States, Events>> withAllRegions = stateMachine.getStateMachineAccessor().withAllRegions();
		for (StateMachineAccess<States, Events> a : withAllRegions) {
			a.resetStateMachine(new DefaultStateMachineContext<>(state, null, null, null));
		}
		stateMachine.start();
	}

	@Test
	public void exchange_change_state_on_handle_event() {
		final String uuid = "xch-1";
		Exchange exchange = new Exchange(uuid);
		Message<Events> message = MessageBuilder
				.withPayload(Events.ACCEPT)
				.setHeaderIfAbsent("exchange", exchange)
				.build();

		boolean result = exchangeService.handleEvent(message);

		exchange = getExchange(uuid);
		assertThat(result, is(true));
		assertThat(exchange.getCurrentState(), is(READY));
	}

	private Exchange getExchange(String uuid) {
		return exchangeService.getExchange(uuid);
	}

}
