package com.order.service.currency;

import java.util.Map;

public class ExchangeResponse {

    private String base;
    private String date;
    private Map<String, Double> rates;

    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
}
