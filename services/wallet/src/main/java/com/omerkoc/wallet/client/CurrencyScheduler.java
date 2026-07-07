package com.omerkoc.wallet.client;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrencyScheduler {
    private final CurrencyApiClient currencyApiClient;
    private final CurrencyService currencyService;

    @Scheduled(fixedDelay = 300000)
    public void refreshCurrencies() {
        var rates = currencyApiClient.fetchLatestRates();
        currencyService.updateCache(rates);
    }
}