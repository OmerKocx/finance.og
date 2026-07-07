package com.omerkoc.wallet.client;

import org.springframework.stereotype.Service;

import com.omerkoc.wallet.dto.CurrencyRateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyService {

    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    public double getRate(String currencyCode) {
        if (currencyCode.equalsIgnoreCase("TRY"))
            return 1.0;

        Double rate = rateCache.get(currencyCode.toUpperCase());
        if (rate == null) {
            log.warn("Kur hafızada bulunamadı: {}, varsayılan el ile girilen kur dönülüyor.", currencyCode);
            return currencyCode.equalsIgnoreCase("USD") ? 34.20 : 37.10;
        }
        return rate;
    }

    public void updateCache(List<CurrencyRateDto> rates) {
        if (rates.isEmpty())
            return;

        rates.forEach(dto -> rateCache.put(dto.code().toUpperCase(), dto.selling()));
        log.info("Döviz kurları RAM üzerinde güncellendi.");
    }
}