package com.br.strutz.order_book.domain.port.input;

import com.br.strutz.order_book.application.query.GetWalletQuery;
import com.br.strutz.order_book.application.query.WalletSnapshot;

public interface GetWalletUseCase {

    WalletSnapshot handle(GetWalletQuery query);
}
