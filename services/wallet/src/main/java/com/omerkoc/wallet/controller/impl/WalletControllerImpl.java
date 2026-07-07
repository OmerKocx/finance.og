package com.omerkoc.wallet.controller.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.omerkoc.wallet.controller.IWalletController;
import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.dto.WalletRequestDto;
import com.omerkoc.wallet.dto.WalletResponseDto;
import com.omerkoc.wallet.service.IWalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/wallets/api/v1")
@RequiredArgsConstructor
public class WalletControllerImpl implements IWalletController {

    private final IWalletService walletService;

    @Override
    @GetMapping("/get/{id}")
    public ResponseEntity<WalletResponseDto> getWalletById(@Valid @PathVariable("id") Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @Override
    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletResponseDto> getWalletByUserId(@Valid @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @Override
    @PostMapping("/create")
    public ResponseEntity<WalletResponseDto> createWallet(@Valid @RequestBody WalletRequestDto walletRequestDto) {
        return ResponseEntity.ok(walletService.createWallet(walletRequestDto));
    }

    @Override
    @PutMapping("/update")
    public ResponseEntity<WalletResponseDto> updateWallet(@Valid @RequestBody WalletRequestDto walletRequestDto) {
        return ResponseEntity.ok(walletService.updateWallet(walletRequestDto));
    }

    @Override
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteWallet(@Valid @PathVariable("id") Long id) {
        walletService.deleteWallet(id);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/list")
    public ResponseEntity<Page<WalletResponseDto>> getAllWallets(Pageable pageable) {
        return ResponseEntity.ok(walletService.getAllWallets(pageable));
    }

    @Override
    @PutMapping("/{walletId}/balance")
    public ResponseEntity<WalletResponseDto> updateBalance(@Valid @PathVariable("walletId") Long walletId,
            @RequestParam("amount") double amount) {
        return ResponseEntity.ok(walletService.updateBalance(walletId, amount));
    }

    @Override
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<WalletResponseDto> depositMoney(@Valid @PathVariable("walletId") Long walletId,
            @RequestParam("amount") double amount) {
        return ResponseEntity.ok(walletService.depositMoney(walletId, amount));
    }

    @Override
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<WalletResponseDto> withdrawMoney(@Valid @PathVariable("walletId") Long walletId,
            @RequestParam("amount") double amount) {
        return ResponseEntity.ok(walletService.withdrawMoney(walletId, amount));
    }

    @Override
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferMoney(@Valid @RequestParam("sourceWalletId") Long sourceWalletId,
            @Valid @RequestParam("destinationWalletId") Long destinationWalletId,
            @Valid @RequestParam("amount") double amount) {
        walletService.transferMoney(sourceWalletId, destinationWalletId, amount);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/{walletId}/history")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionHistory(
            @Valid @PathVariable("walletId") Long walletId, Pageable pageable) {
        return ResponseEntity.ok(walletService.getTransactionHistory(walletId, pageable));
    }

    @Override
    @GetMapping("/{walletId}/history/date")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryByDate(
            @Valid @PathVariable("walletId") Long walletId,
            @Valid @RequestParam("startDate") String startDate,
            @Valid @RequestParam("endDate") String endDate,
            Pageable pageable) {
        return ResponseEntity.ok(walletService.getTransactionHistoryByDate(walletId, startDate, endDate, pageable));
    }

    @Override
    @GetMapping("/{walletId}/history/type")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionHistoryByType(
            @Valid @PathVariable("walletId") Long walletId,
            @Valid @RequestParam("type") String type,
            Pageable pageable) {
        return ResponseEntity.ok(walletService.getTransactionHistoryByType(walletId, type, pageable));
    }
}