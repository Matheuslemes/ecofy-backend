package br.com.ecofy.ms_budgeting.adapters.in.sched;

import br.com.ecofy.ms_budgeting.core.application.command.RecalculateBudgetsCommand;
import br.com.ecofy.ms_budgeting.core.port.in.RecalculateBudgetsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BudgetRecalculationSchedulerTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-08-11T12:00:00Z");

    private static final LocalDate FIXED_DATE =
            LocalDate.of(2026, 8, 11);

    @Mock
    private RecalculateBudgetsUseCase useCase;

    private BudgetRecalculationScheduler scheduler;

    @BeforeEach
    void setUp() {
        // Arrange
        Clock clock = Clock.fixed(
                FIXED_INSTANT,
                ZoneOffset.UTC
        );

        scheduler = new BudgetRecalculationScheduler(
                useCase,
                clock
        );
    }

    @Test
    @DisplayName("Deve executar o recálculo global utilizando a data atual")
    void shouldExecuteGlobalRecalculationUsingCurrentDate() {
        // Arrange
        ArgumentCaptor<RecalculateBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(RecalculateBudgetsCommand.class);

        // Act
        scheduler.recalc();

        // Assert
        verify(useCase).recalculate(commandCaptor.capture());

        RecalculateBudgetsCommand command = commandCaptor.getValue();

        assertThat(command).isNotNull();
        assertThat(command.runId()).isNotNull();
        assertThat(command.userId()).isNull();
        assertThat(command.referenceDate()).isEqualTo(FIXED_DATE);
    }

    @Test
    @DisplayName("Deve utilizar a data fornecida pelo relógio configurado")
    void shouldUseDateFromConfiguredClock() {
        // Arrange
        Clock customClock = Clock.fixed(
                Instant.parse("2030-12-25T18:30:00Z"),
                ZoneOffset.UTC
        );

        BudgetRecalculationScheduler customScheduler =
                new BudgetRecalculationScheduler(
                        useCase,
                        customClock
                );

        ArgumentCaptor<RecalculateBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(RecalculateBudgetsCommand.class);

        // Act
        customScheduler.recalc();

        // Assert
        verify(useCase).recalculate(commandCaptor.capture());

        RecalculateBudgetsCommand command = commandCaptor.getValue();

        assertThat(command.referenceDate())
                .isEqualTo(LocalDate.of(2030, 12, 25));

        assertThat(command.runId()).isNotNull();
        assertThat(command.userId()).isNull();
    }

    @Test
    @DisplayName("Deve gerar um novo identificador para cada execução do recálculo")
    void shouldGenerateNewRunIdForEachRecalculation() {
        // Arrange
        ArgumentCaptor<RecalculateBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(RecalculateBudgetsCommand.class);

        // Act
        scheduler.recalc();
        scheduler.recalc();

        // Assert
        verify(useCase, times(2))
                .recalculate(commandCaptor.capture());

        RecalculateBudgetsCommand firstCommand =
                commandCaptor.getAllValues().get(0);

        RecalculateBudgetsCommand secondCommand =
                commandCaptor.getAllValues().get(1);

        assertThat(firstCommand.runId()).isNotNull();
        assertThat(secondCommand.runId()).isNotNull();
        assertThat(firstCommand.runId())
                .isNotEqualTo(secondCommand.runId());

        assertThat(firstCommand.referenceDate())
                .isEqualTo(FIXED_DATE);

        assertThat(secondCommand.referenceDate())
                .isEqualTo(FIXED_DATE);
    }

    @Test
    @DisplayName("Deve capturar exceção do caso de uso sem propagá-la")
    void shouldCatchUseCaseExceptionWithoutPropagating() {
        // Arrange
        RuntimeException exception =
                new RuntimeException("Falha ao recalcular orçamentos");

        doThrow(exception)
                .when(useCase)
                .recalculate(any(RecalculateBudgetsCommand.class));

        // Act
        scheduler.recalc();

        // Assert
        verify(useCase)
                .recalculate(any(RecalculateBudgetsCommand.class));
    }

    @Test
    @DisplayName("Deve permitir nova execução após falha no recálculo anterior")
    void shouldAllowNewExecutionAfterPreviousFailure() {
        // Arrange
        RuntimeException exception =
                new RuntimeException("Falha temporária");

        doThrow(exception)
                .doNothing()
                .when(useCase)
                .recalculate(any(RecalculateBudgetsCommand.class));

        // Act
        scheduler.recalc();
        scheduler.recalc();

        // Assert
        verify(useCase, times(2))
                .recalculate(any(RecalculateBudgetsCommand.class));
    }

    @Test
    @DisplayName("Deve criar comando global sem usuário específico")
    void shouldCreateGlobalCommandWithoutSpecificUser() {
        // Arrange
        ArgumentCaptor<RecalculateBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(RecalculateBudgetsCommand.class);

        // Act
        scheduler.recalc();

        // Assert
        verify(useCase).recalculate(commandCaptor.capture());

        RecalculateBudgetsCommand command = commandCaptor.getValue();

        assertThat(command.userId()).isNull();
        assertThat(command.runId()).isInstanceOf(UUID.class);
        assertThat(command.referenceDate()).isEqualTo(FIXED_DATE);
    }
}
