package com.omerkoc.wallet.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletResponseDto;

public interface IWalletController {

    // --- TEMEL CÜZDAN CRUD İŞLEMLERİ ---
    ResponseEntity<WalletResponseDto> getWalletById(Long id);

    ResponseEntity<WalletResponseDto> getWalletByUserId(Long userId);

    ResponseEntity<WalletResponseDto> createWallet(Long userId);

    ResponseEntity<WalletResponseDto> updateWallet(Long userId, WalletResponseDto walletResponseDto);

    ResponseEntity<Void> deleteWallet(Long userId);

    ResponseEntity<Page<WalletResponseDto>> getAllWallets(Pageable pageable);

    // --- BAKIYE VE PARA HAREKETLERİ ---
    ResponseEntity<WalletResponseDto> updateBalance(Long walletId, double amount);

    ResponseEntity<WalletResponseDto> depositMoney(Long walletId, double amount);

    ResponseEntity<WalletResponseDto> withdrawMoney(Long walletId, double amount);

    // Transfer sonucunda cüzdan yerine "İşlem başarılı" (Void) dönmek en doğrusudur
    ResponseEntity<Void> transferMoney(Long sourceWalletId, Long destinationWalletId, double amount);

    // --- İŞLEM GEÇMİŞİ (TRANSACTION HISTORY) ---
    // Geçmiş işlemler cüzdan DTO'su değil, Transaction DTO listesi dönmelidir!
    ResponseEntity<Page<TransactionResponseDto>> getTransactionHistory(Long walletId, Pageable pageable);

    ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryByDate(Long walletId, String startDate,
            String endDate, Pageable pageable);

    ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryByType(Long walletId, String type, Pageable pageable);
}