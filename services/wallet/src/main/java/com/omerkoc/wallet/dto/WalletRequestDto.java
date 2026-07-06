package com.omerkoc.wallet.dto;

import com.omerkoc.wallet.enums.Currency;
import com.omerkoc.wallet.enums.Status;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record WalletRequestDto(
                @NotNull(message = "User ID cannot be null") Long userId,

                @PositiveOrZero(message = "Initial balance must be zero or positive") double balance,

                @NotNull(message = "Currency cannot be null") Currency currency,

                @NotNull(message = "Wallet status cannot be null") Status status) {
}