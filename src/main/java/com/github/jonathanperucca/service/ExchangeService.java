package com.github.jonathanperucca.service;

import org.springframework.stereotype.Service;

import static com.github.jonathanperucca.model.States.READY;

@Service
public class ExchangeService {

    public void onEnterAccept() {
        System.out.println("ExchangeService : set status = " + READY);
    }

    public void onExitAccept() {
        System.out.println("ExchangeService : leaving status " + READY);
    }
}