package com.omerkoc.wallet.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.omerkoc.wallet.enums.TransactionType;
import com.omerkoc.wallet.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByWalletId(Long walletId, Pageable pageable);

    Page<Transaction> findByWalletIdAndType(Long walletId, TransactionType type, Pageable pageable);

    Page<Transaction> findByWalletIdAndCreatedDateBetween(Long walletId, LocalDateTime start, LocalDateTime end,
            Pageable pageable);
}
