package com.github.jonathanperucca;

import com.github.jonathanperucca.handler.PersistHandlerConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
		PersistHandlerConfig.class,
		StateMachineConfig.class
})
public class ApplicationTest {
}