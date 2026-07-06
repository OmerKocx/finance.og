package com.omerkoc.wallet.enums;

public enum Currency {
    TL("TRY"),
    USD("USD"),
    EUR("EUR");

    private final String isoCode;

    Currency(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getIsoCode() {
        return this.isoCode;
    }
}