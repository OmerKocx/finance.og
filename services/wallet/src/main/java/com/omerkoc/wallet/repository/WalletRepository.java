package com.omerkoc.wallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.omerkoc.wallet.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
