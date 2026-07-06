package com.omerkoc.wallet.service.impl;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletRequestDto;
import com.omerkoc.wallet.dto.WalletResponseDto;
import com.omerkoc.wallet.exception.WalletAlreadyExistsException;
import com.omerkoc.wallet.exception.WalletNotFoundException;
import com.omerkoc.wallet.mapper.WalletMapper;
import com.omerkoc.wallet.model.Wallet;
import com.omerkoc.wallet.service.IWalletService;

import jakarta.transaction.Transactional;

import com.omerkoc.wallet.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements IWalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper mapper;

    @Override
    public WalletResponseDto getWalletById(Long id) {
        log.info("Fetching wallet by id: {}", id);
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + id));
        return mapper.toWalletResponseDto(wallet);
    }

    @Override
    public WalletResponseDto getWalletByUserId(Long userId) {
        log.info("Fetching wallet by userId: {}", userId);
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with userId: " + userId));
        return mapper.toWalletResponseDto(wallet);
    }

    @Override
    public WalletResponseDto createWallet(WalletRequestDto walletRequestDto) {
        walletRepository.findByUserId(walletRequestDto.userId())
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException(
                            "Wallet already exists with userId: " + walletRequestDto.userId());
                });
        log.info("Creating wallet for userId: {}", walletRequestDto.userId());
        Wallet wallet = mapper.toWallet(walletRequestDto);
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    @Override
    public WalletResponseDto updateWallet(WalletRequestDto walletRequestDto) {
        walletRepository.findByUserId(walletRequestDto.userId())
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException(
                            "Wallet already exists with userId: " + walletRequestDto.userId());
                });
        log.info("Updating wallet for userId: {}", walletRequestDto.userId());
        Wallet wallet = walletRepository.findByUserId(walletRequestDto.userId())
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found with userId: " + walletRequestDto.userId()));
        wallet.setBalance(walletRequestDto.balance());
        wallet.setCurrency(walletRequestDto.currency());
        wallet.setStatus(walletRequestDto.status());
        wallet.setUpdatedDate(LocalDateTime.now());
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    @Override
    public void deleteWallet(Long id) {
        walletRepository.findById(id)
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException("Wallet not found with id: " + id);
                });
        log.info("Deleting wallet for id: {}", id);
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + id));
        walletRepository.delete(wallet);
    }

    @Override
    public Page<WalletResponseDto> getAllWallets(Pageable pageable) {
        log.info("Fetching all wallets with pagination: {}", pageable);
        return walletRepository.findAll(pageable).map(mapper::toWalletResponseDto);
    }

    @Override
    public WalletResponseDto updateBalance(Long walletId, double amount) {
        walletRepository.findById(walletId)
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException("Wallet not found with id: " + walletId);
                });
        log.info("Updating balance for walletId: {} with amount: {}", walletId, amount);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));
        wallet.setBalance(amount);
        wallet.setUpdatedDate(LocalDateTime.now());
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    @Override
    public WalletResponseDto depositMoney(Long walletId, double amount) {
        walletRepository.findById(walletId)
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException("Wallet not found with id: " + walletId);
                });
        log.info("Depositing amount: {} to walletId: {}", amount, walletId);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));
        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedDate(LocalDateTime.now());
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    @Override
    @Transactional
    public WalletResponseDto withdrawMoney(Long walletId, double amount) {
        walletRepository.findById(walletId)
                .ifPresent(wallet -> {
                    throw new WalletAlreadyExistsException("Wallet not found with id: " + walletId);
                });
        log.info("Withdrawing amount: {} from walletId: {}", amount, walletId);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));
        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setUpdatedDate(LocalDateTime.now());
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    @Override
    @Transactional
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
