package com.github.jonathanperucca.service;

import com.github.jonathanperucca.model.Exchange;

import java.util.HashMap;
import java.util.Map;

public class MapDB {

    private Map<String, Exchange> exchangeDB;
    public static final String EXCHANGE_ONE = "xch-prd-1";
    public static final String EXCHANGE_TWO = "xch-prd-2";

    public MapDB() {
        exchangeDB = new HashMap<>(2);
        exchangeDB.put(EXCHANGE_ONE, new Exchange(EXCHANGE_ONE));
        exchangeDB.put(EXCHANGE_TWO, new Exchange(EXCHANGE_TWO));
    }

    public void showDB() {
        exchangeDB.forEach((id, exchange) -> System.out.println("exchange (" + id + ") has state : " + exchange.getCurrentState()));
    }

    public Exchange getExchange(String uuid) {
        return exchangeDB.get(uuid);
    }

    public Exchange putExchange(String uuid, Exchange exchange) {
        return exchangeDB.put(uuid, exchange);
    }
}
