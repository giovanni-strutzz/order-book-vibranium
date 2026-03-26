package com.br.strutz.order_book.domain.port.output.wallet;

import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.user.UserId;

import java.util.Optional;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findByUserId(UserId userId);

    boolean existsByUserId(UserId userId);
}