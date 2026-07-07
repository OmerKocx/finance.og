package com.omerkoc.wallet.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletRequestDto;
import com.omerkoc.wallet.dto.WalletResponseDto;

public interface IWalletController {

    ResponseEntity<WalletResponseDto> getWalletById(Long id);

    ResponseEntity<WalletResponseDto> getWalletByUserId(Long userId);

    ResponseEntity<WalletResponseDto> createWallet(WalletRequestDto walletRequestDto);

    ResponseEntity<WalletResponseDto> updateWallet(WalletRequestDto walletRequestDto);

    ResponseEntity<Void> deleteWallet(Long walletId);

    ResponseEntity<Page<WalletResponseDto>> getAllWallets(Pageable pageable);

    ResponseEntity<WalletResponseDto> updateBalance(Long walletId, double amount);

    ResponseEntity<WalletResponseDto> depositMoney(Long walletId, double amount);

    ResponseEntity<WalletResponseDto> withdrawMoney(Long walletId, double amount);

    ResponseEntity<Void> transferMoney(Long sourceWalletId, Long destinationWalletId, double amount);

    ResponseEntity<Page<TransactionResponseDto>> getTransactionHistory(Long walletId, Pageable pageable);

    ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryByDate(Long walletId, String startDate,
            String endDate, Pageable pageable);

    ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryByType(Long walletId, String type,
            Pageable pageable);
}