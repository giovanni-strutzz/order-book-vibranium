package com.br.strutz.order_book.domain.service;

import com.br.strutz.order_book.domain.exception.WalletAlreadyExistsException;
import com.br.strutz.order_book.domain.exception.WalletNotFoundException;
import com.br.strutz.order_book.domain.model.Money;
import com.br.strutz.order_book.domain.model.aggregates.Trade;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.order.OrderType;
import com.br.strutz.order_book.domain.model.user.UserId;
import com.br.strutz.order_book.domain.port.output.wallet.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletService")
class WalletServiceTest {

    @Mock private WalletRepository  repository;
    @Mock private RedissonClient    redissonClient;
    @Mock private RLock             rLock;

    @InjectMocks
    private WalletService walletService;

    

    private UserId userId;
    private Wallet wallet;
    private Money price;
    private Money  quantity;

    @BeforeEach
    void setUp() throws InterruptedException {
        userId   = mock(UserId.class);
        wallet   = mock(Wallet.class);
        price    = mock(Money.class);
        quantity = mock(Money.class);

        lenient().when(userId.getValue()).thenReturn("user-123");
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        lenient().when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        lenient().when(repository.findByUserId(userId)).thenReturn(Optional.of(wallet));
    }

    

    @Nested
    @DisplayName("createWallet()")
    class CreateWallet {

        @Test
        @DisplayName("deve criar e persistir carteira quando usuário não possui uma")
        void shouldCreateAndPersistWalletWhenUserHasNone() {
            Money  initialBalance = mock(Money.class);
            Wallet newWallet      = mock(Wallet.class);

            when(repository.existsByUserId(userId)).thenReturn(false);
            when(repository.save(any(Wallet.class))).thenReturn(newWallet);

            Wallet result = walletService.createWallet(userId, initialBalance);

            assertThat(result).isSameAs(newWallet);
            verify(repository).existsByUserId(userId);
            verify(repository).save(any(Wallet.class));
        }

        @Test
        @DisplayName("deve lançar WalletAlreadyExistsException quando carteira já existe")
        void shouldThrowWhenWalletAlreadyExists() {
            when(repository.existsByUserId(userId)).thenReturn(true);

            assertThatThrownBy(() -> walletService.createWallet(userId, mock(Money.class)))
                    .isInstanceOf(WalletAlreadyExistsException.class);

            verify(repository, never()).save(any());
        }
    }

    

    @Nested
    @DisplayName("reserveForBuyOrder()")
    class ReserveForBuyOrder {

        @Test
        @DisplayName("deve adquirir lock, reservar e salvar carteira")
        void shouldAcquireLockReserveAndSave() throws InterruptedException {
            walletService.reserveForBuyOrder(userId, price, quantity);

            verify(rLock).tryLock(anyLong(), anyLong(), any());
            verify(wallet).reserveForBuyOrder(price, quantity);
            verify(repository).save(wallet);
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("deve lançar IllegalStateException quando lock não é adquirido")
        void shouldThrowWhenLockNotAcquired() throws InterruptedException {
            when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

            assertThatThrownBy(() -> walletService.reserveForBuyOrder(userId, price, quantity))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Could not acquire lock");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar WalletNotFoundException quando carteira não existe")
        void shouldThrowWalletNotFoundWhenWalletMissing() {
            when(repository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.reserveForBuyOrder(userId, price, quantity))
                    .isInstanceOf(WalletNotFoundException.class);

            verify(repository, never()).save(any());
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("deve lançar IllegalStateException quando thread é interrompida")
        void shouldThrowAndSetInterruptFlagWhenInterrupted() throws InterruptedException {
            when(rLock.tryLock(anyLong(), anyLong(), any())).thenThrow(new InterruptedException());

            assertThatThrownBy(() -> walletService.reserveForBuyOrder(userId, price, quantity))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Lock interrupted");

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            
            Thread.interrupted();
        }
    }

    

    @Nested
    @DisplayName("reserveForSellOrder()")
    class ReserveForSellOrder {

        @Test
        @DisplayName("deve adquirir lock, reservar e salvar carteira")
        void shouldAcquireLockReserveAndSave() throws InterruptedException {
            walletService.reserveForSellOrder(userId, quantity);

            verify(rLock).tryLock(anyLong(), anyLong(), any());
            verify(wallet).reserveForSellOrder(quantity);
            verify(repository).save(wallet);
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("deve lançar IllegalStateException quando lock não é adquirido")
        void shouldThrowWhenLockNotAcquired() throws InterruptedException {
            when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

            assertThatThrownBy(() -> walletService.reserveForSellOrder(userId, quantity))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Could not acquire lock");
        }

        @Test
        @DisplayName("deve lançar WalletNotFoundException quando carteira não existe")
        void shouldThrowWalletNotFoundWhenWalletMissing() {
            when(repository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.reserveForSellOrder(userId, quantity))
                    .isInstanceOf(WalletNotFoundException.class);

            verify(rLock).unlock();
        }
    }

    

    @Nested
    @DisplayName("settleTrades()")
    class SettleTrades {

        private Trade buildTrade(UserId buyerId, UserId sellerId) {
            Trade trade = mock(Trade.class);
            Money total = mock(Money.class);
            Money qty   = mock(Money.class);
            Money prc   = mock(Money.class);

            lenient().when(trade.getBuyerId()).thenReturn(buyerId);
            lenient().when(trade.getSellerId()).thenReturn(sellerId);
            lenient().when(trade.getTotalValue()).thenReturn(total);
            lenient().when(trade.getQuantity()).thenReturn(qty);
            lenient().when(trade.getPrice()).thenReturn(prc);

            return trade;
        }

        @Test
        @DisplayName("deve liquidar comprador e vendedor para cada trade")
        void shouldSettleBuyerAndSellerForEachTrade() {
            UserId buyerId  = mock(UserId.class);
            UserId sellerId = mock(UserId.class);
            Wallet buyerWallet  = mock(Wallet.class);
            Wallet sellerWallet = mock(Wallet.class);

            lenient().when(buyerId.getValue()).thenReturn("buyer-1");
            lenient().when(sellerId.getValue()).thenReturn("seller-1");
            lenient().when(redissonClient.getLock("wallet-lock:buyer-1")).thenReturn(rLock);
            lenient().when(redissonClient.getLock("wallet-lock:seller-1")).thenReturn(rLock);
            lenient().when(repository.findByUserId(buyerId)).thenReturn(Optional.of(buyerWallet));
            lenient().when(repository.findByUserId(sellerId)).thenReturn(Optional.of(sellerWallet));

            Trade trade = buildTrade(buyerId, sellerId);

            walletService.settleTrades(List.of(trade));

            
            verify(buyerWallet).debitReserved(any(), anyString());
            verify(buyerWallet).creditFromTrade(any(), anyString());
            
            verify(sellerWallet).creditFromTrade(any(), anyString());
            verify(sellerWallet).debitReserved(any(), anyString());
            verify(repository, times(2)).save(any(Wallet.class));
        }

        @Test
        @DisplayName("deve processar lista vazia sem erros")
        void shouldHandleEmptyTradeList() {
            walletService.settleTrades(List.of());

            verify(repository, never()).save(any());
            verify(redissonClient, never()).getLock(anyString());
        }

        @Test
        @DisplayName("deve processar múltiplos trades em sequência")
        void shouldProcessMultipleTradesSequentially() {
            UserId buyerId  = mock(UserId.class);
            UserId sellerId = mock(UserId.class);

            lenient().when(buyerId.getValue()).thenReturn("buyer-1");
            lenient().when(sellerId.getValue()).thenReturn("seller-1");
            lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
            lenient().when(repository.findByUserId(buyerId)).thenReturn(Optional.of(mock(Wallet.class)));
            lenient().when(repository.findByUserId(sellerId)).thenReturn(Optional.of(mock(Wallet.class)));

            Trade t1 = buildTrade(buyerId, sellerId);
            Trade t2 = buildTrade(buyerId, sellerId);

            walletService.settleTrades(List.of(t1, t2));

            
            verify(repository, times(4)).save(any(Wallet.class));
        }
    }

    

    @Nested
    @DisplayName("releaseReserve()")
    class ReleaseReserve {

        @Test
        @DisplayName("deve liberar reserva multiplicando price × quantity para ordem BUY")
        void shouldReleaseReserveMultipliedForBuyOrder() {
            Money multiplied = mock(Money.class);
            when(price.multiply(quantity)).thenReturn(multiplied);

            walletService.releaseReserve(userId, OrderType.BUY, price, quantity);

            verify(price).multiply(quantity);
            verify(wallet).releaseReserve(multiplied);
            verify(repository).save(wallet);
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("deve liberar reserva com quantity diretamente para ordem SELL")
        void shouldReleaseReserveDirectlyForSellOrder() {
            walletService.releaseReserve(userId, OrderType.SELL, price, quantity);

            verify(wallet).releaseReserve(quantity);
            verify(price, never()).multiply(any());
            verify(repository).save(wallet);
            verify(rLock).unlock();
        }

        @Test
        @DisplayName("deve lançar IllegalStateException quando lock não é adquirido")
        void shouldThrowWhenLockNotAcquired() throws InterruptedException {
            when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(false);

            assertThatThrownBy(() ->
                    walletService.releaseReserve(userId, OrderType.BUY, price, quantity))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Could not acquire lock");
        }
    }

    

    @Nested
    @DisplayName("findWallet()")
    class FindWallet {

        @Test
        @DisplayName("deve retornar carteira quando encontrada")
        void shouldReturnWalletWhenFound() {
            when(repository.findByUserId(userId)).thenReturn(Optional.of(wallet));

            Wallet result = walletService.findWallet(userId);

            assertThat(result).isSameAs(wallet);
            verify(repository).findByUserId(userId);
        }

        @Test
        @DisplayName("deve lançar WalletNotFoundException quando carteira não existe")
        void shouldThrowWalletNotFoundWhenAbsent() {
            when(repository.findByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> walletService.findWallet(userId))
                    .isInstanceOf(WalletNotFoundException.class);
        }
    }
}