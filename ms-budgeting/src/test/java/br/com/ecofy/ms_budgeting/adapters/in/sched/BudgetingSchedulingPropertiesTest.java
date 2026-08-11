package br.com.ecofy.ms_budgeting.adapters.in.sched;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetingSchedulingPropertiesTest {

    @Test
    @DisplayName("Deve possuir valores padrão corretos ao criar as propriedades")
    void shouldHaveCorrectDefaultValues() {
        // Arrange / Act
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Assert
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.isRecalculationEnabled()).isTrue();
        assertThat(properties.isCleanupEnabled()).isFalse();
        assertThat(properties.getCleanupRetentionDays()).isEqualTo(90);
        assertThat(properties.getCleanupCron()).isEqualTo("0 0 3 * * *");
        assertThat(properties.getRecalculateCron()).isEqualTo("0 0/15 * * * *");
    }

    @Test
    @DisplayName("Deve alterar e retornar a propriedade enabled")
    void shouldSetAndGetEnabled() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setEnabled(false);

        // Assert
        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Deve alterar e retornar a propriedade recalculationEnabled")
    void shouldSetAndGetRecalculationEnabled() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setRecalculationEnabled(false);

        // Assert
        assertThat(properties.isRecalculationEnabled()).isFalse();
    }

    @Test
    @DisplayName("Deve alterar e retornar a propriedade cleanupEnabled")
    void shouldSetAndGetCleanupEnabled() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setCleanupEnabled(true);

        // Assert
        assertThat(properties.isCleanupEnabled()).isTrue();
    }

    @Test
    @DisplayName("Deve alterar e retornar o período de retenção da limpeza")
    void shouldSetAndGetCleanupRetentionDays() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setCleanupRetentionDays(120);

        // Assert
        assertThat(properties.getCleanupRetentionDays()).isEqualTo(120);
    }

    @Test
    @DisplayName("Deve permitir período de retenção igual a zero")
    void shouldAllowZeroCleanupRetentionDays() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setCleanupRetentionDays(0);

        // Assert
        assertThat(properties.getCleanupRetentionDays()).isZero();
    }

    @Test
    @DisplayName("Deve permitir período de retenção negativo sem validação")
    void shouldAllowNegativeCleanupRetentionDays() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setCleanupRetentionDays(-1);

        // Assert
        assertThat(properties.getCleanupRetentionDays()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Deve alterar e retornar a expressão cron da limpeza")
    void shouldSetAndGetCleanupCron() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        String cron = "0 30 2 * * *";

        // Act
        properties.setCleanupCron(cron);

        // Assert
        assertThat(properties.getCleanupCron()).isEqualTo(cron);
    }

    @Test
    @DisplayName("Deve permitir expressão cron da limpeza nula")
    void shouldAllowNullCleanupCron() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setCleanupCron(null);

        // Assert
        assertThat(properties.getCleanupCron()).isNull();
    }

    @Test
    @DisplayName("Deve permitir expressão cron da limpeza vazia")
    void shouldAllowEmptyCleanupCron() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setCleanupCron("");

        // Assert
        assertThat(properties.getCleanupCron()).isEmpty();
    }

    @Test
    @DisplayName("Deve alterar e retornar a expressão cron de recálculo")
    void shouldSetAndGetRecalculateCron() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        String cron = "0 */30 * * * *";

        // Act
        properties.setRecalculateCron(cron);

        // Assert
        assertThat(properties.getRecalculateCron()).isEqualTo(cron);
    }

    @Test
    @DisplayName("Deve permitir expressão cron de recálculo nula")
    void shouldAllowNullRecalculateCron() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setRecalculateCron(null);

        // Assert
        assertThat(properties.getRecalculateCron()).isNull();
    }

    @Test
    @DisplayName("Deve permitir expressão cron de recálculo vazia")
    void shouldAllowEmptyRecalculateCron() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setRecalculateCron("");

        // Assert
        assertThat(properties.getRecalculateCron()).isEmpty();
    }

    @Test
    @DisplayName("Deve permitir alterar todas as propriedades simultaneamente")
    void shouldSetAllProperties() {
        // Arrange
        BudgetingSchedulingProperties properties =
                new BudgetingSchedulingProperties();

        // Act
        properties.setEnabled(false);
        properties.setRecalculationEnabled(false);
        properties.setCleanupEnabled(true);
        properties.setCleanupRetentionDays(365);
        properties.setCleanupCron("0 0 1 * * *");
        properties.setRecalculateCron("0 */5 * * * *");

        // Assert
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.isRecalculationEnabled()).isFalse();
        assertThat(properties.isCleanupEnabled()).isTrue();
        assertThat(properties.getCleanupRetentionDays()).isEqualTo(365);
        assertThat(properties.getCleanupCron()).isEqualTo("0 0 1 * * *");
        assertThat(properties.getRecalculateCron()).isEqualTo("0 */5 * * * *");
    }
}
