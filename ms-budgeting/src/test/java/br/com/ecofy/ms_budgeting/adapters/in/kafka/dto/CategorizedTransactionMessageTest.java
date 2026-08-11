package br.com.ecofy.ms_budgeting.adapters.in.kafka.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategorizedTransactionMessageTest {

    @Test
    @DisplayName("Deve criar mensagem com todos os campos preenchidos")
    void shouldCreateMessageWithAllFields() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("150.75");
        String currency = "BRL";
        LocalDate transactionDate = LocalDate.of(2026, 8, 11);
        MessageMetadata metadata = null;

        // Act
        CategorizedTransactionMessage message = new CategorizedTransactionMessage(
                transactionId,
                userId,
                categoryId,
                amount,
                currency,
                transactionDate,
                metadata
        );

        // Assert
        assertThat(message.transactionId()).isEqualTo(transactionId);
        assertThat(message.userId()).isEqualTo(userId);
        assertThat(message.categoryId()).isEqualTo(categoryId);
        assertThat(message.amount()).isEqualByComparingTo(amount);
        assertThat(message.currency()).isEqualTo(currency);
        assertThat(message.transactionDate()).isEqualTo(transactionDate);
        assertThat(message.metadata()).isNull();
    }

    @Test
    @DisplayName("Deve permitir criação da mensagem com todos os campos nulos")
    void shouldAllowCreationWithAllFieldsNull() {
        // Arrange

        // Act
        CategorizedTransactionMessage message = new CategorizedTransactionMessage(
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Assert
        assertThat(message.transactionId()).isNull();
        assertThat(message.userId()).isNull();
        assertThat(message.categoryId()).isNull();
        assertThat(message.amount()).isNull();
        assertThat(message.currency()).isNull();
        assertThat(message.transactionDate()).isNull();
        assertThat(message.metadata()).isNull();
    }

    @Test
    @DisplayName("Deve considerar mensagens iguais quando todos os campos forem iguais")
    void shouldConsiderMessagesEqualWhenAllFieldsAreEqual() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("99.90");
        String currency = "BRL";
        LocalDate transactionDate = LocalDate.of(2026, 8, 11);

        CategorizedTransactionMessage first = new CategorizedTransactionMessage(
                transactionId,
                userId,
                categoryId,
                amount,
                currency,
                transactionDate,
                null
        );

        CategorizedTransactionMessage second = new CategorizedTransactionMessage(
                transactionId,
                userId,
                categoryId,
                amount,
                currency,
                transactionDate,
                null
        );

        // Act / Assert
        assertThat(first)
                .isEqualTo(second)
                .hasSameHashCodeAs(second);
    }

    @Test
    @DisplayName("Deve considerar a própria instância igual a ela mesma")
    void shouldBeEqualToItself() {
        // Arrange
        CategorizedTransactionMessage message = new CategorizedTransactionMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.TEN,
                "BRL",
                LocalDate.of(2026, 8, 11),
                null
        );

        // Act / Assert
        assertThat(message).isEqualTo(message);
    }

    @Test
    @DisplayName("Deve considerar mensagens diferentes quando algum campo for diferente")
    void shouldConsiderMessagesDifferentWhenFieldDiffers() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        LocalDate transactionDate = LocalDate.of(2026, 8, 11);

        CategorizedTransactionMessage first = new CategorizedTransactionMessage(
                transactionId,
                userId,
                categoryId,
                new BigDecimal("100.00"),
                "BRL",
                transactionDate,
                null
        );

        CategorizedTransactionMessage second = new CategorizedTransactionMessage(
                transactionId,
                userId,
                categoryId,
                new BigDecimal("200.00"),
                "BRL",
                transactionDate,
                null
        );

        // Act / Assert
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    @DisplayName("Deve ser diferente de nulo e de objeto de outro tipo")
    void shouldNotBeEqualToNullOrDifferentType() {
        // Arrange
        CategorizedTransactionMessage message = new CategorizedTransactionMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ONE,
                "BRL",
                LocalDate.of(2026, 8, 11),
                null
        );

        // Act / Assert
        assertThat(message).isNotEqualTo(null);
        assertThat(message).isNotEqualTo("outro-objeto");
    }

    @Test
    @DisplayName("Deve gerar representação textual contendo os valores dos campos")
    void shouldGenerateToStringWithFieldValues() {
        // Arrange
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("42.50");
        String currency = "USD";
        LocalDate transactionDate = LocalDate.of(2026, 8, 10);

        CategorizedTransactionMessage message = new CategorizedTransactionMessage(
                transactionId,
                userId,
                categoryId,
                amount,
                currency,
                transactionDate,
                null
        );

        // Act
        String result = message.toString();

        // Assert
        assertThat(result)
                .contains("CategorizedTransactionMessage")
                .contains(transactionId.toString())
                .contains(userId.toString())
                .contains(categoryId.toString())
                .contains(amount.toString())
                .contains(currency)
                .contains(transactionDate.toString())
                .contains("metadata=null");
    }
}
