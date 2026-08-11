package br.com.ecofy.ms_budgeting.adapters.in.sched;

import br.com.ecofy.ms_budgeting.core.application.command.CleanupBudgetsCommand;
import br.com.ecofy.ms_budgeting.core.port.in.CleanupBudgetsUseCase;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetCleanupSchedulerTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-08-11T12:00:00Z");

    private static final LocalDate FIXED_DATE =
            LocalDate.of(2026, 8, 11);

    @Mock
    private CleanupBudgetsUseCase useCase;

    @Mock
    private BudgetingSchedulingProperties props;

    private Clock clock;

    private BudgetCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

        scheduler = new BudgetCleanupScheduler(
                useCase,
                props,
                clock
        );
    }

    @Test
    @DisplayName("Deve executar limpeza quando o agendamento estiver habilitado")
    void shouldExecuteCleanupWhenEnabled() {
        // Arrange
        int retentionDays = 90;

        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(retentionDays);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        ArgumentCaptor<CleanupBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(CleanupBudgetsCommand.class);

        // Act
        scheduler.cleanup();

        // Assert
        verify(useCase).cleanup(commandCaptor.capture());

        CleanupBudgetsCommand command = commandCaptor.getValue();

        assertThat(command.runId()).isNotNull();
        assertThat(command.referenceDate()).isEqualTo(FIXED_DATE);
        assertThat(command.retentionDays()).isEqualTo(retentionDays);

        verify(props).isCleanupEnabled();
        verify(props).getCleanupRetentionDays();
        verifyNoMoreInteractions(useCase);
    }

    @Test
    @DisplayName("Deve ignorar limpeza quando o agendamento estiver desabilitado")
    void shouldSkipCleanupWhenDisabled() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(false);

        // Act
        scheduler.cleanup();

        // Assert
        verify(props).isCleanupEnabled();
        verify(props, never()).getCleanupRetentionDays();
        verifyNoInteractions(useCase);
    }

    @Test
    @DisplayName("Deve impedir uma segunda execução enquanto a limpeza já estiver em andamento")
    void shouldSkipSecondExecutionWhenCleanupIsAlreadyRunning() throws Exception {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(30);

        CountDownLatch cleanupStarted = new CountDownLatch(1);
        CountDownLatch releaseCleanup = new CountDownLatch(1);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation -> {
                    cleanupStarted.countDown();

                    if (!releaseCleanup.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timeout aguardando liberação do teste"
                        );
                    }

                    return mock(invocation.getMethod().getReturnType());
                });

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            Future<?> firstExecution = executor.submit(scheduler::cleanup);

            assertThat(cleanupStarted.await(5, TimeUnit.SECONDS))
                    .isTrue();

            // Act
            scheduler.cleanup();

            // Assert
            verify(useCase, times(1))
                    .cleanup(any(CleanupBudgetsCommand.class));

            releaseCleanup.countDown();

            firstExecution.get(5, TimeUnit.SECONDS);

            verify(useCase, times(1))
                    .cleanup(any(CleanupBudgetsCommand.class));
        } finally {
            releaseCleanup.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Deve capturar exceção da limpeza sem propagá-la")
    void shouldHandleCleanupExceptionWithoutPropagating() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(60);

        RuntimeException exception =
                new RuntimeException("Falha ao limpar orçamentos");

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenThrow(exception);

        // Act
        scheduler.cleanup();

        // Assert
        verify(useCase).cleanup(any(CleanupBudgetsCommand.class));
    }

    @Test
    @DisplayName("Deve liberar nova execução após ocorrer uma exceção")
    void shouldAllowNewExecutionAfterException() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(45);

        RuntimeException exception =
                new RuntimeException("Falha temporária");

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenThrow(exception)
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        // Act
        scheduler.cleanup();
        scheduler.cleanup();

        // Assert
        verify(useCase, times(2))
                .cleanup(any(CleanupBudgetsCommand.class));
    }

    @Test
    @DisplayName("Deve liberar nova execução após concluir a limpeza com sucesso")
    void shouldAllowNewExecutionAfterSuccessfulCleanup() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(30);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        // Act
        scheduler.cleanup();
        scheduler.cleanup();

        // Assert
        verify(useCase, times(2))
                .cleanup(any(CleanupBudgetsCommand.class));
    }

    @Test
    @DisplayName("Deve gerar um identificador de execução diferente a cada limpeza")
    void shouldGenerateDifferentRunIdForEachCleanup() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(30);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        ArgumentCaptor<CleanupBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(CleanupBudgetsCommand.class);

        // Act
        scheduler.cleanup();
        scheduler.cleanup();

        // Assert
        verify(useCase, times(2))
                .cleanup(commandCaptor.capture());

        assertThat(commandCaptor.getAllValues())
                .hasSize(2);

        assertThat(commandCaptor.getAllValues().get(0).runId())
                .isNotNull()
                .isNotEqualTo(commandCaptor.getAllValues().get(1).runId());

        assertThat(commandCaptor.getAllValues().get(1).runId())
                .isNotNull();
    }

    @Test
    @DisplayName("Deve utilizar a data atual do relógio configurado como referência")
    void shouldUseCurrentDateFromConfiguredClock() {
        // Arrange
        Clock customClock = Clock.fixed(
                Instant.parse("2030-12-25T15:30:00Z"),
                ZoneOffset.UTC
        );

        BudgetCleanupScheduler customScheduler =
                new BudgetCleanupScheduler(
                        useCase,
                        props,
                        customClock
                );

        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(120);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        ArgumentCaptor<CleanupBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(CleanupBudgetsCommand.class);

        // Act
        customScheduler.cleanup();

        // Assert
        verify(useCase).cleanup(commandCaptor.capture());

        assertThat(commandCaptor.getValue().referenceDate())
                .isEqualTo(LocalDate.of(2030, 12, 25));

        assertThat(commandCaptor.getValue().retentionDays())
                .isEqualTo(120);

        assertThat(commandCaptor.getValue().runId())
                .isNotNull();
    }

    @Test
    @DisplayName("Deve encaminhar zero como período de retenção quando configurado")
    void shouldForwardZeroRetentionDaysWhenConfigured() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays()).thenReturn(0);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        ArgumentCaptor<CleanupBudgetsCommand> commandCaptor =
                ArgumentCaptor.forClass(CleanupBudgetsCommand.class);

        // Act
        scheduler.cleanup();

        // Assert
        verify(useCase).cleanup(commandCaptor.capture());

        assertThat(commandCaptor.getValue().retentionDays())
                .isZero();
    }

    @Test
    @DisplayName("Deve liberar nova execução após erro causado por período de retenção negativo")
    void shouldAllowNewExecutionAfterInvalidRetentionDays() {
        // Arrange
        when(props.isCleanupEnabled()).thenReturn(true);
        when(props.getCleanupRetentionDays())
                .thenReturn(-1)
                .thenReturn(30);

        when(useCase.cleanup(any(CleanupBudgetsCommand.class)))
                .thenAnswer(invocation ->
                        mock(invocation.getMethod().getReturnType())
                );

        // Act
        scheduler.cleanup();
        scheduler.cleanup();

        // Assert
        verify(props, times(2)).isCleanupEnabled();
        verify(props, times(2)).getCleanupRetentionDays();

        verify(useCase, times(1))
                .cleanup(any(CleanupBudgetsCommand.class));
    }
}
