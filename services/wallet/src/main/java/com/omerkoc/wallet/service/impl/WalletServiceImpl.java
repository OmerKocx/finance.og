package com.omerkoc.wallet.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletResponseDto;
import com.omerkoc.wallet.service.IWalletService;
//import com.omerkoc.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements IWalletService {

    // private final WalletRepository walletRepository;

    @Override
    public WalletResponseDto getWalletById(Long id) {
        log.info("Fetching wallet by id: {}", id);
        return null;
    }

    @Override
    public WalletResponseDto getWalletByUserId(Long userId) {
        log.info("Fetching wallet by userId: {}", userId);
        return null;
    }

    @Override
    public WalletResponseDto createWallet(Long userId) {
        log.info("Creating wallet for userId: {}", userId);
        return null;
    }

    @Override
    public WalletResponseDto updateWallet(Long userId, WalletResponseDto walletResponseDto) {
        log.info("Updating wallet for userId: {}", userId);
        return null;
    }

    @Override
    public void deleteWallet(Long userId) {
        log.info("Deleting wallet for userId: {}", userId);
    }

    @Override
    public Page<WalletResponseDto> getAllWallets(Pageable pageable) {
        log.info("Fetching all wallets with pagination: {}", pageable);
        return null;
    }

    @Override
    public WalletResponseDto updateBalance(Long walletId, double amount) {
        log.info("Updating balance for walletId: {} with amount: {}", walletId, amount);
        return null;
    }

    @Override
    public WalletResponseDto depositMoney(Long walletId, double amount) {
        log.info("Depositing amount: {} to walletId: {}", amount, walletId);
        return null;
    }

    @Override
    public WalletResponseDto withdrawMoney(Long walletId, double amount) {
        log.info("Withdrawing amount: {} from walletId: {}", amount, walletId);
        return null;
    }

    @Override
    public void transferMoney(Long sourceWalletId, Long destinationWalletId, double amount) {
        log.info("Transferring amount: {} from sourceWalletId: {} to destinationWalletId: {}", amount, sourceWalletId,
                destinationWalletId);
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistory(Long walletId, Pageable pageable) {
        log.info("Fetching transaction history for walletId: {} with pagination: {}", walletId, pageable);
        return null;
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistoryByDate(Long walletId, String startDate, String endDate,
            Pageable pageable) {
        log.info("Fetching transaction history for walletId: {} between {} and {} with pagination: {}", walletId,
                startDate, endDate, pageable);
        return null;
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistoryByType(Long walletId, String type, Pageable pageable) {
        log.info("Fetching transaction history for walletId: {} of type: {} with pagination: {}", walletId, type,
                pageable);
        return null;
    }
}
