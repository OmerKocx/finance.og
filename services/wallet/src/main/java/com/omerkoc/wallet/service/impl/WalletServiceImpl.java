package com.omerkoc.wallet.service.impl;

import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletRequestDto;
import com.omerkoc.wallet.dto.WalletResponseDto;
import com.omerkoc.wallet.exception.WalletAlreadyExistsException;
import com.omerkoc.wallet.exception.WalletNotFoundException;
import com.omerkoc.wallet.exception.InsufficientBalanceException;
import com.omerkoc.wallet.mapper.WalletMapper;
import com.omerkoc.wallet.model.Wallet;
import com.omerkoc.wallet.service.IWalletService;
import com.omerkoc.wallet.repository.WalletRepository;
import com.omerkoc.wallet.enums.TransactionType;
import com.omerkoc.wallet.mapper.TransactionMapper;
import com.omerkoc.wallet.model.Transaction;
import com.omerkoc.wallet.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WalletServiceImpl implements IWalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper mapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    public WalletResponseDto getWalletById(Long id) {
        log.info("Fetching wallet by id: {}", id);
        return walletRepository.findById(id)
                .map(mapper::toWalletResponseDto)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + id));
    }

    @Override
    public WalletResponseDto getWalletByUserId(Long userId) {
        log.info("Fetching wallet by userId: {}", userId);
        return walletRepository.findByUserId(userId)
                .map(mapper::toWalletResponseDto)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with userId: " + userId));
    }

    @Override
    @Transactional
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
    @Transactional
    public WalletResponseDto updateWallet(WalletRequestDto walletRequestDto) {
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
    @Transactional
    public void deleteWallet(Long id) {
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
    @Transactional
    public WalletResponseDto updateBalance(Long walletId, double amount) {
        log.info("Updating balance for walletId: {} to exact amount: {}", walletId, amount);
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));
        wallet.setBalance(amount);
        wallet.setUpdatedDate(LocalDateTime.now());
        return mapper.toWalletResponseDto(walletRepository.save(wallet));
    }

    @Override
    @Transactional
    public WalletResponseDto depositMoney(Long walletId, double amount) {
        log.info("Depositing amount: {} to walletId: {}", amount, walletId);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setUpdatedDate(LocalDateTime.now());
        WalletResponseDto response = mapper.toWalletResponseDto(walletRepository.save(wallet));

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .description("Deposited " + amount + " " + wallet.getCurrency())
                .createdDate(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return response;
    }

    @Override
    @Transactional
    public WalletResponseDto withdrawMoney(Long walletId, double amount) {
        log.info("Withdrawing amount: {} from walletId: {}", amount, walletId);
        Wallet wallet = walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id: " + walletId));

        if (wallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Insufficient balance in wallet id: " + walletId);
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setUpdatedDate(LocalDateTime.now());
        WalletResponseDto response = mapper.toWalletResponseDto(walletRepository.save(wallet));

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .amount(amount)
                .type(TransactionType.WITHDRAW)
                .description("Withdrew " + amount + " " + wallet.getCurrency())
                .createdDate(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return response;
    }

    @Override
    @Transactional
    public void transferMoney(Long sourceWalletId, Long destinationWalletId, double amount) {
        log.info("Transferring amount: {} from {} to {}", amount, sourceWalletId, destinationWalletId);

        Wallet sourceWallet = walletRepository.findByIdForUpdate(sourceWalletId)
                .orElseThrow(() -> new WalletNotFoundException("Source wallet not found"));

        if (sourceWallet.getBalance() < amount) {
            throw new InsufficientBalanceException("Transfer failed. Insufficient balance!");
        }

        Wallet destinationWallet = walletRepository.findByIdForUpdate(destinationWalletId)
                .orElseThrow(() -> new WalletNotFoundException("Destination wallet not found"));

        sourceWallet.setBalance(sourceWallet.getBalance() - amount);
        destinationWallet.setBalance(destinationWallet.getBalance() + amount);

        sourceWallet.setUpdatedDate(LocalDateTime.now());
        destinationWallet.setUpdatedDate(LocalDateTime.now());

        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        Transaction outTx = Transaction.builder()
                .walletId(sourceWalletId)
                .amount(amount)
                .type(TransactionType.TRANSFER_OUT)
                .description("Transferred " + amount + " " + sourceWallet.getCurrency() + " to wallet ID: "
                        + destinationWalletId)
                .createdDate(LocalDateTime.now())
                .build();
        transactionRepository.save(outTx);

        Transaction inTx = Transaction.builder()
                .walletId(destinationWalletId)
                .amount(amount)
                .type(TransactionType.TRANSFER_IN)
                .description("Received " + amount + " " + destinationWallet.getCurrency() + " from wallet ID: "
                        + sourceWalletId)
                .createdDate(LocalDateTime.now())
                .build();
        transactionRepository.save(inTx);
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistory(Long walletId, Pageable pageable) {
        log.info("Fetching transaction history for walletId: {} with pageable: {}", walletId, pageable);
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException("Wallet not found with id: " + walletId);
        }
        return transactionRepository.findByWalletId(walletId, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistoryByDate(Long walletId, String startDate, String endDate,
            Pageable pageable) {
        log.info("Fetching transaction history for walletId: {} between {} and {}", walletId, startDate, endDate);
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException("Wallet not found with id: " + walletId);
        }

        LocalDateTime start = parseDateTime(startDate, false);
        LocalDateTime end = parseDateTime(endDate, true);

        if (start == null || end == null) {
            throw new IllegalArgumentException("Invalid date range parameters");
        }

        return transactionRepository.findByWalletIdAndCreatedDateBetween(walletId, start, end, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }

    @Override
    public Page<TransactionResponseDto> getTransactionHistoryByType(Long walletId, String type, Pageable pageable) {
        log.info("Fetching transaction history for walletId: {} of type: {}", walletId, type);
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException("Wallet not found with id: " + walletId);
        }

        TransactionType transactionType;
        try {
            transactionType = TransactionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }

        return transactionRepository.findByWalletIdAndType(walletId, transactionType, pageable)
                .map(transactionMapper::toTransactionResponseDto);
    }

    private LocalDateTime parseDateTime(String dateStr, boolean isEnd) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            } else {
                java.time.LocalDate localDate = java.time.LocalDate.parse(dateStr);
                return isEnd ? localDate.atTime(java.time.LocalTime.MAX) : localDate.atStartOfDay();
            }
        } catch (Exception e) {
            log.warn("Failed to parse date string: {}", dateStr, e);
            return null;
        }
    }
}