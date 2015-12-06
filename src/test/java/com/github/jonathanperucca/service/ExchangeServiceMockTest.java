package com.github.jonathanperucca.service;

import com.github.jonathanperucca.exception.BusinessStateMachineException;
import com.github.jonathanperucca.handler.PersistStateMachineHandler;
import com.github.jonathanperucca.model.Exchange;
import com.github.jonathanperucca.service.ExchangeService.HandleEventRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeServiceMockTest {

    @InjectMocks
    ExchangeService exchangeService;

    @Mock
    PersistStateMachineHandler handler;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    HandleEventRequest mockRequest;

    @Mock
    Exchange mockExchange;

    @Test
    public void should_extract_and_set_exchange_to_request() {
        when(mockRequest.getMessage().getHeaders().get(any(), any())).thenReturn(mockExchange);

        exchangeService.extractExchange().apply(mockRequest);

        verify(mockRequest).setExchange(any(Exchange.class));
    }

    @Test
    public void should_return_false_when_delegation_raise_business_exception() {
        when(mockRequest.getExchange()).thenReturn(mockExchange);
        when(handler.handleEventWithState(any(), any())).thenThrow(BusinessStateMachineException.class);

        boolean result = exchangeService.delegateMessageToHandler(mockRequest);

        assertThat(result, is(false));
    }
}
