package com.omerkoc.wallet.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletResponseDto;

public interface IWalletService {

    WalletResponseDto getWalletById(Long id);

    WalletResponseDto getWalletByUserId(Long userId);

    WalletResponseDto createWallet(Long userId);

    WalletResponseDto updateWallet(Long userId, WalletResponseDto walletResponseDto);

    void deleteWallet(Long userId);

    Page<WalletResponseDto> getAllWallets(Pageable pageable);

    WalletResponseDto updateBalance(Long walletId, double amount);

    WalletResponseDto depositMoney(Long walletId, double amount);

    WalletResponseDto withdrawMoney(Long walletId, double amount);

    void transferMoney(Long sourceWalletId, Long destinationWalletId, double amount);

    Page<TransactionResponseDto> getTransactionHistory(Long walletId, Pageable pageable);

    Page<TransactionResponseDto> getTransactionHistoryByDate(Long walletId, String startDate, String endDate, Pageable pageable);

    Page<TransactionResponseDto> getTransactionHistoryByType(Long walletId, String type, Pageable pageable);
}
