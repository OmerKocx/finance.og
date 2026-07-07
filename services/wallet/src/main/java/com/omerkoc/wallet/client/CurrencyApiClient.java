package com.omerkoc.wallet.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.omerkoc.wallet.dto.CurrencyRateDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrencyApiClient {
    private final RestTemplate restTemplate;
    private final String URL = "https://api.doviz.com/v1/currencies/all/latest";

    public List<CurrencyRateDto> fetchLatestRates() {
        try {
            CurrencyRateDto[] response = restTemplate.getForObject(URL, CurrencyRateDto[].class);
            return response != null ? Arrays.asList(response) : Collections.emptyList();
        } catch (Exception e) {
            // API patlarsa sistem çökmesin diye log atıp boş liste dönüyoruz
            return Collections.emptyList();
        }
    }
}
