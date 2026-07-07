package com.omerkoc.wallet.mapper;

import org.springframework.stereotype.Component;

import com.omerkoc.wallet.dto.WalletRequestDto;
import com.omerkoc.wallet.dto.WalletResponseDto;
import com.omerkoc.wallet.model.Wallet;

@Component
public class WalletMapper {

    public Wallet toWallet(WalletRequestDto walletRequestDto) {
        return Wallet.builder()
                .userId(walletRequestDto.userId())
                .balance(walletRequestDto.balance())
                .currency(walletRequestDto.currency())
                .status(walletRequestDto.status())
                .build();
    }

    public WalletResponseDto toWalletResponseDto(Wallet wallet) {
        return WalletResponseDto.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .createdDate(wallet.getCreatedDate())
                .updatedDate(wallet.getUpdatedDate())
                .build();
    }
}