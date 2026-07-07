package com.omerkoc.wallet.mapper;

import org.springframework.stereotype.Component;

import com.omerkoc.wallet.dto.TransactionResponseDto;
import com.omerkoc.wallet.model.Transaction;

@Component
public class TransactionMapper {

    public TransactionResponseDto toTransactionResponseDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .walletId(transaction.getWalletId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .createdDate(transaction.getCreatedDate())
                .build();
    }
}