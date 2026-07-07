package com.omerkoc.wallet.dto;

import java.time.LocalDateTime;

import com.omerkoc.wallet.enums.Currency;
import com.omerkoc.wallet.enums.Status;

import lombok.Builder;

@Builder
public record WalletResponseDto(
                Long id,
                Long userId,
                double balance,
                Currency currency,
                Status status,
                LocalDateTime createdDate,
                LocalDateTime updatedDate) {
}