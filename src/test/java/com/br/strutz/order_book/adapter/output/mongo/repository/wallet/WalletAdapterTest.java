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

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private WalletAdapter walletAdapter;

    private UserId      userId;
    private Wallet      domainWallet;
    private WalletDocument walletDocument;

    @BeforeEach
    void setUp() {
        userId        = mock(UserId.class);
        domainWallet  = mock(Wallet.class);
        walletDocument = mock(WalletDocument.class);

        lenient().when(userId.getValue()).thenReturn("user-123");
    }

    
    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve inserir nova wallet quando não existe documento no banco")
        void shouldInsertWhenWalletDoesNotExist() {
            WalletDocument insertedDoc  = mock(WalletDocument.class);
            Wallet         resultDomain = mock(Wallet.class);

            when(walletDocument.getUserId()).thenReturn("user-123");
            when(mapper.toDocument(domainWallet)).thenReturn(walletDocument);

            
            when(mongoTemplate.findOne(any(Query.class), eq(WalletDocument.class)))
                    .thenReturn(null);
            when(mongoTemplate.insert(walletDocument)).thenReturn(insertedDoc);
            when(mapper.toDomain(insertedDoc)).thenReturn(resultDomain);

            Wallet result = walletAdapter.save(domainWallet);

            assertThat(result).isSameAs(resultDomain);
            verify(mongoTemplate).insert(walletDocument);
            verify(mongoTemplate, never()).findAndReplace(any(), any());
        }

        @Test
        @DisplayName("deve substituir wallet existente via findAndReplace quando já existe")
        void shouldReplaceWhenWalletAlreadyExists() {
            WalletDocument existingDoc = buildExistingDoc("existing-id", "user-123");
            WalletDocument newDoc      = buildNewDoc("user-123");
            Wallet         resultDomain = mock(Wallet.class);

            when(mapper.toDocument(domainWallet)).thenReturn(newDoc);

            
            when(mongoTemplate.findOne(any(Query.class), eq(WalletDocument.class)))
                    .thenReturn(existingDoc);
            when(mapper.toDomain(any(WalletDocument.class))).thenReturn(resultDomain);

            Wallet result = walletAdapter.save(domainWallet);

            assertThat(result).isSameAs(resultDomain);
            verify(mongoTemplate).findAndReplace(any(Query.class), any(WalletDocument.class));
            verify(mongoTemplate, never()).insert(any(WalletDocument.class));
        }

        @Test
        @DisplayName("deve montar WalletDocument de substituição com id e createdAt do existente")
        void shouldBuildReplacementDocumentWithExistingIdAndCreatedAt() {
            Instant            originalCreatedAt = Instant.parse("2024-01-01T10:00:00Z");
            Instant            newUpdatedAt      = Instant.parse("2024-06-01T12:00:00Z");

            WalletDocument existingDoc = WalletDocument.builder()
                    .id("existing-id")
                    .userId("user-123")
                    .createdAt(originalCreatedAt)
                    .build();

            WalletDocument newDoc = WalletDocument.builder()
                    .userId("user-123")
                    .availableBalance(BigDecimal.TEN)
                    .reservedBalance(BigDecimal.ONE)
                    .transactions(List.of())
                    .updatedAt(newUpdatedAt)
                    .build();

            when(mapper.toDocument(domainWallet)).thenReturn(newDoc);
            when(mongoTemplate.findOne(any(Query.class), eq(WalletDocument.class)))
                    .thenReturn(existingDoc);

            ArgumentCaptor<WalletDocument> replacedCaptor =
                    ArgumentCaptor.forClass(WalletDocument.class);
            when(mapper.toDomain(replacedCaptor.capture())).thenReturn(domainWallet);

            walletAdapter.save(domainWallet);

            WalletDocument replaced = replacedCaptor.getValue();
            assertThat(replaced.getId()).isEqualTo("existing-id");
            assertThat(replaced.getUserId()).isEqualTo("user-123");
            assertThat(replaced.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(replaced.getUpdatedAt()).isEqualTo(newUpdatedAt);
            assertThat(replaced.getLockTouchedAt()).isEqualTo(newUpdatedAt);
            assertThat(replaced.getAvailableBalance()).isEqualByComparingTo(BigDecimal.TEN);
        }
    }

    
    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("deve retornar Optional com domínio quando wallet existe")
        void shouldReturnWalletWhenFound() {
            WalletDocument doc          = mock(WalletDocument.class);
            Wallet         resultDomain = mock(Wallet.class);

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

    
    @Nested
    @DisplayName("findDocumentByUserId() — comportamento interno via save()")
    class FindDocumentByUserId {

        @Test
        @DisplayName("deve montar Query com campo user_id correto")
        void shouldQueryByUserIdField() {
            WalletDocument doc = mock(WalletDocument.class);
            when(doc.getUserId()).thenReturn("user-123");
            when(mapper.toDocument(domainWallet)).thenReturn(doc);

            ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);

            when(mongoTemplate.findOne(queryCaptor.capture(), eq(WalletDocument.class)))
                    .thenReturn(null);
            when(mongoTemplate.insert(doc)).thenReturn(doc);
            when(mapper.toDomain(doc)).thenReturn(domainWallet);

            walletAdapter.save(domainWallet);

            Query query = queryCaptor.getValue();
            
            assertThat(query.getQueryObject().getString("user_id")).isEqualTo("user-123");
        }
    }

    
    private WalletDocument buildExistingDoc(String id, String userId) {
        return WalletDocument.builder()
                .id(id)
                .userId(userId)
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();
    }

    private WalletDocument buildNewDoc(String userId) {
        return WalletDocument.builder()
                .userId(userId)
                .availableBalance(BigDecimal.valueOf(100))
                .reservedBalance(BigDecimal.ZERO)
                .transactions(List.of())
                .updatedAt(Instant.now())
                .build();
    }
}