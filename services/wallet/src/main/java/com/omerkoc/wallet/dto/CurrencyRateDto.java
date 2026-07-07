package com.omerkoc.wallet.dto;

import lombok.Builder;

@Builder
public record CurrencyRateDto(String code, double buying, double selling) {
}