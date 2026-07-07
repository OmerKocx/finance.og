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

    // Kurları RAM'de tutacağımız thread-safe harita (Cache)
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    // Sistemdeki diğer servisler (Wallet, Order vb.) kuru sadece buradan çağıracak!
    public double getRate(String currencyCode) {
        if (currencyCode.equalsIgnoreCase("TRY"))
            return 1.0; // TL ise zaten 1'dir.

        Double rate = rateCache.get(currencyCode.toUpperCase());
        if (rate == null) {
            log.warn("Kur hafızada bulunamadı: {}, varsayılan el ile girilen kur dönülüyor.", currencyCode);
            return currencyCode.equalsIgnoreCase("USD") ? 34.20 : 37.10; // Fallback (B planı) önlemi
        }
        return rate;
    }

    // Bu metodu sadece 4. adımdaki zamanlayıcı tetikleyecek
    public void updateCache(List<CurrencyRateDto> rates) {
        if (rates.isEmpty())
            return;

        rates.forEach(dto -> rateCache.put(dto.code().toUpperCase(), dto.selling()));
        log.info("Döviz kurları RAM üzerinde güncellendi.");
    }
}
