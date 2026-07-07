package com.omerkoc.wallet.dto;

import java.time.LocalDateTime;
import com.omerkoc.wallet.enums.TransactionType;
import lombok.Builder;

@Builder
public record TransactionResponseDto(
        Long id,
        Long walletId,
        double amount,
        TransactionType type,
        String description,
        LocalDateTime createdDate
) {}