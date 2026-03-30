package com.br.strutz.order_book.adapter.output.mongo.repository.wallet;

import com.br.strutz.order_book.adapter.output.mongo.adapter.WalletAdapter;
import com.br.strutz.order_book.adapter.output.mongo.document.WalletDocument;
import com.br.strutz.order_book.adapter.output.mongo.mapper.WalletMapper;
import com.br.strutz.order_book.domain.model.aggregates.Wallet;
import com.br.strutz.order_book.domain.model.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletAdapter")
class WalletAdapterTest {

    @Mock
    private WalletMongoRepository mongoRepository;

    @Mock
    private WalletMapper mapper;

    @InjectMocks
    private WalletAdapter walletAdapter;

    private UserId userId;
    private Wallet domainWallet;
    private WalletDocument walletDocument;

    @BeforeEach
    void setUp() {
        userId = mock(UserId.class);
        domainWallet = mock(Wallet.class);
        walletDocument = mock(WalletDocument.class);

        lenient().when(userId.getValue()).thenReturn("user-123");
    }

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve salvar wallet e retornar domínio")
        void shouldSaveWallet() {
            WalletDocument savedDoc = mock(WalletDocument.class);
            Wallet resultDomain = mock(Wallet.class);

            when(mapper.toDocument(domainWallet)).thenReturn(walletDocument);
            when(mongoRepository.save(walletDocument)).thenReturn(savedDoc);
            when(mapper.toDomain(savedDoc)).thenReturn(resultDomain);

            Wallet result = walletAdapter.save(domainWallet);

            assertThat(result).isSameAs(resultDomain);
            verify(mongoRepository).save(walletDocument);
        }
    }

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("deve retornar Optional com domínio quando wallet existe")
        void shouldReturnWalletWhenFound() {
            WalletDocument doc = mock(WalletDocument.class);
            Wallet resultDomain = mock(Wallet.class);

            when(mongoRepository.findByUserId("user-123")).thenReturn(Optional.of(doc));
            when(mapper.toDomain(doc)).thenReturn(resultDomain);

            Optional<Wallet> result = walletAdapter.findByUserId(userId);

            assertThat(result).isPresent().contains(resultDomain);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando wallet não existe")
        void shouldReturnEmptyWhenNotFound() {
            when(mongoRepository.findByUserId("user-123")).thenReturn(Optional.empty());

            Optional<Wallet> result = walletAdapter.findByUserId(userId);

            assertThat(result).isEmpty();
            verify(mapper, never()).toDomain(any());
        }
    }

    @Nested
    @DisplayName("existsByUserId()")
    class ExistsByUserId {

        @Test
        @DisplayName("deve retornar true quando wallet existe para o usuário")
        void shouldReturnTrueWhenExists() {
            when(mongoRepository.existsByUserId("user-123")).thenReturn(true);

            assertThat(walletAdapter.existsByUserId(userId)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando wallet não existe para o usuário")
        void shouldReturnFalseWhenNotExists() {
            when(mongoRepository.existsByUserId("user-123")).thenReturn(false);

            assertThat(walletAdapter.existsByUserId(userId)).isFalse();
        }
    }
}