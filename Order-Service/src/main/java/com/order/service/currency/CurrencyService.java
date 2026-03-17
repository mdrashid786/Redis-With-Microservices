package com.order.service.currency;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CurrencyService {

    public Double getUsdToInr() {

        String url = "https://api.exchangerate-api.com/v4/latest/USD";

        RestTemplate restTemplate = new RestTemplate();

        ExchangeResponse response =
                restTemplate.getForObject(url, ExchangeResponse.class);

        return response.getRates().get("INR");
    }
}