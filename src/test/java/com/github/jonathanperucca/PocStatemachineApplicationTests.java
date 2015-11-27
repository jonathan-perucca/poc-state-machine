package com.github.jonathanperucca;

import com.github.jonathanperucca.model.Events;
import com.github.jonathanperucca.model.States;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.github.jonathanperucca.model.Events.ACCEPT;
import static com.github.jonathanperucca.model.States.PENDING;
import static com.github.jonathanperucca.model.States.READY;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApplicationTest.class)
public class PocStatemachineApplicationTests {

	@Autowired
	StateMachine<States, Events> stateMachine;

	@Test
	public void simple_state_transition() throws Exception {
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

}
