package br.com.ecofy.ms_budgeting.adapters.in.web;

import br.com.ecofy.ms_budgeting.adapters.in.web.dto.request.CreateBudgetRequest;
import br.com.ecofy.ms_budgeting.adapters.in.web.dto.request.UpdateBudgetRequest;
import br.com.ecofy.ms_budgeting.adapters.in.web.dto.response.BudgetOverviewResponse;
import br.com.ecofy.ms_budgeting.adapters.in.web.dto.response.BudgetResponse;
import br.com.ecofy.ms_budgeting.adapters.in.web.security.AuthenticatedUser;
import br.com.ecofy.ms_budgeting.adapters.in.web.support.MoneyCents;
import br.com.ecofy.ms_budgeting.config.BudgetingProperties;
import br.com.ecofy.ms_budgeting.core.application.command.CreateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.command.DeleteBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.command.UpdateBudgetCommand;
import br.com.ecofy.ms_budgeting.core.application.exception.PaginationParameterInvalidException;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetResult;
import br.com.ecofy.ms_budgeting.core.domain.exception.BudgetAccessForbiddenException;
import br.com.ecofy.ms_budgeting.core.port.in.CreateBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.DeleteBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.GetBudgetOverviewUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.GetBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.ListBudgetsUseCase;
import br.com.ecofy.ms_budgeting.core.port.in.UpdateBudgetUseCase;
import br.com.ecofy.ms_budgeting.core.port.out.PageResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetControllerTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID BUDGET_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID CATEGORY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final String OWNER_CLAIM = "sub";
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Mock
    private CreateBudgetUseCase createBudgetUseCase;

    @Mock
    private UpdateBudgetUseCase updateBudgetUseCase;

    @Mock
    private DeleteBudgetUseCase deleteBudgetUseCase;

    @Mock
    private ListBudgetsUseCase listBudgetsUseCase;

    @Mock
    private GetBudgetUseCase getBudgetUseCase;

    @Mock
    private GetBudgetOverviewUseCase getBudgetOverviewUseCase;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private BudgetingProperties props;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private BudgetController controller;

    @Test
    @DisplayName("Deve criar orçamento e retornar status 201 com Location")
    void shouldCreateBudgetAndReturnCreated() {
        // Arrange
        String idempotencyKey = "idem-create-123";
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);
        CreateBudgetRequest request = mock(CreateBudgetRequest.class);
        BudgetResult created = mock(BudgetResult.class);
        BudgetResponse expectedBody = mock(BudgetResponse.class);

        when(request.categoryId()).thenReturn(CATEGORY_ID);
        when(request.periodStart()).thenReturn(start);
        when(request.periodEnd()).thenReturn(end);
        when(request.limitAmountCents()).thenReturn(10_000L);
        when(request.currency()).thenReturn("BRL");
        when(created.id()).thenReturn(BUDGET_ID);
        when(createBudgetUseCase.create(any(CreateBudgetCommand.class), eq(idempotencyKey))).thenReturn(created);

        MockHttpServletRequest servletRequest = new MockHttpServletRequest(
                "POST", "/api/budgeting/v1/budgets");
        servletRequest.setScheme("http");
        servletRequest.setServerName("localhost");
        servletRequest.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner();
             MockedStatic<BudgetResponse> responseFactory = mockStatic(BudgetResponse.class)) {
            responseFactory.when(() -> BudgetResponse.from(created)).thenReturn(expectedBody);

            // Act
            var response = controller.create(idempotencyKey, request);

            // Assert
            ArgumentCaptor<CreateBudgetCommand> captor = ArgumentCaptor.forClass(CreateBudgetCommand.class);
            verify(createBudgetUseCase).create(captor.capture(), eq(idempotencyKey));

            CreateBudgetCommand command = captor.getValue();
            assertThat(command.userId()).isEqualTo(OWNER_ID);
            assertThat(command.categoryId()).isEqualTo(CATEGORY_ID);
            assertThat(command.periodStart()).isEqualTo(start);
            assertThat(command.periodEnd()).isEqualTo(end);
            assertThat(command.limitAmount()).isEqualTo(MoneyCents.fromCents(10_000L));
            assertThat(command.currency()).isEqualTo("BRL");
            assertThat(response.getStatusCode().value()).isEqualTo(201);
            assertThat(response.getBody()).isSameAs(expectedBody);
            assertThat(response.getHeaders().getLocation()).isNotNull();
            assertThat(response.getHeaders().getLocation().getPath()).endsWith("/" + BUDGET_ID);
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    @DisplayName("Deve atualizar orçamento convertendo o novo limite informado em centavos")
    void shouldUpdateBudgetWithNewLimit() {
        // Arrange
        String idempotencyKey = "idem-update-123";
        UpdateBudgetRequest request = mock(UpdateBudgetRequest.class);
        BudgetResult current = ownedBudget();
        BudgetResult updated = mock(BudgetResult.class);
        BudgetResponse expectedBody = mock(BudgetResponse.class);

        when(request.newLimitAmountCents()).thenReturn(25_000L);
        when(request.currency()).thenReturn("BRL");
        when(request.version()).thenReturn(3L);
        when(getBudgetUseCase.get(BUDGET_ID)).thenReturn(current);
        when(updateBudgetUseCase.update(any(UpdateBudgetCommand.class), eq(idempotencyKey))).thenReturn(updated);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner();
             MockedStatic<BudgetResponse> responseFactory = mockStatic(BudgetResponse.class)) {
            responseFactory.when(() -> BudgetResponse.from(updated)).thenReturn(expectedBody);

            // Act
            var response = controller.update(idempotencyKey, BUDGET_ID, request);

            // Assert
            ArgumentCaptor<UpdateBudgetCommand> captor = ArgumentCaptor.forClass(UpdateBudgetCommand.class);
            verify(updateBudgetUseCase).update(captor.capture(), eq(idempotencyKey));

            UpdateBudgetCommand command = captor.getValue();
            assertThat(command.budgetId()).isEqualTo(BUDGET_ID);
            assertThat(command.newLimitAmount()).isEqualTo(MoneyCents.fromCents(25_000L));
            assertThat(command.currency()).isEqualTo("BRL");
            assertThat(command.expectedVersion()).isEqualTo(3L);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isSameAs(expectedBody);
        }
    }

    @Test
    @DisplayName("Deve atualizar orçamento mantendo novo limite nulo quando não informado")
    void shouldUpdateBudgetWithoutNewLimit() {
        // Arrange
        String idempotencyKey = "idem-update-456";
        UpdateBudgetRequest request = mock(UpdateBudgetRequest.class);
        BudgetResult current = ownedBudget();
        BudgetResult updated = mock(BudgetResult.class);
        BudgetResponse expectedBody = mock(BudgetResponse.class);

        when(request.currency()).thenReturn(null);
        when(request.version()).thenReturn(4L);
        when(getBudgetUseCase.get(BUDGET_ID)).thenReturn(current);
        when(updateBudgetUseCase.update(any(UpdateBudgetCommand.class), eq(idempotencyKey))).thenReturn(updated);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner();
             MockedStatic<BudgetResponse> responseFactory = mockStatic(BudgetResponse.class)) {
            responseFactory.when(() -> BudgetResponse.from(updated)).thenReturn(expectedBody);

            // Act
            var response = controller.update(idempotencyKey, BUDGET_ID, request);

            // Assert
            ArgumentCaptor<UpdateBudgetCommand> captor = ArgumentCaptor.forClass(UpdateBudgetCommand.class);
            verify(updateBudgetUseCase).update(captor.capture(), eq(idempotencyKey));
            assertThat(captor.getValue().newLimitAmount()).isNull();
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isSameAs(expectedBody);
        }
    }

    @Test
    @DisplayName("Deve impedir atualização quando o orçamento pertencer a outro usuário")
    void shouldRejectUpdateWhenBudgetBelongsToAnotherUser() {
        // Arrange
        String idempotencyKey = "idem-update-789";
        UpdateBudgetRequest request = mock(UpdateBudgetRequest.class);
        BudgetResult current = mock(BudgetResult.class);
        Counter counter = mock(Counter.class);

        when(current.userId()).thenReturn(OTHER_OWNER_ID);
        when(getBudgetUseCase.get(BUDGET_ID)).thenReturn(current);
        when(meterRegistry.counter(
                "ecofy.budgeting.ownership.denied.total", "operation", "budget"))
                .thenReturn(counter);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.update(idempotencyKey, BUDGET_ID, request);

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(BudgetAccessForbiddenException.class);

            verify(getBudgetUseCase).get(BUDGET_ID);
            verify(counter).increment();
            verifyNoInteractions(updateBudgetUseCase);
        }
    }

    @Test
    @DisplayName("Deve remover orçamento pertencente ao usuário e retornar status 204")
    void shouldDeleteOwnedBudget() {
        // Arrange
        String idempotencyKey = "idem-delete-123";
        BudgetResult current = ownedBudget();
        when(getBudgetUseCase.get(BUDGET_ID)).thenReturn(current);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            var response = controller.delete(idempotencyKey, BUDGET_ID);

            // Assert
            ArgumentCaptor<DeleteBudgetCommand> captor = ArgumentCaptor.forClass(DeleteBudgetCommand.class);
            verify(deleteBudgetUseCase).delete(captor.capture(), eq(idempotencyKey));
            assertThat(captor.getValue().budgetId()).isEqualTo(BUDGET_ID);
            assertThat(response.getStatusCode().value()).isEqualTo(204);
            assertThat(response.getBody()).isNull();
        }
    }

    @Test
    @DisplayName("Deve listar usando paginação e ordenação padrão quando parâmetros forem nulos")
    void shouldListWithDefaultPaginationAndSort() {
        // Arrange
        stubPaginationDefaults();
        var expectedQuery = new ListBudgetsUseCase.ListBudgetsQuery(
                OWNER_ID, 0, DEFAULT_SIZE, "createdAt", false);
        when(listBudgetsUseCase.list(expectedQuery))
                .thenReturn(new PageResult<>(List.of(), 0, DEFAULT_SIZE, 0L));

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            var response = controller.list(null, null, null);

            // Assert
            verify(listBudgetsUseCase).list(expectedQuery);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
        }
    }

    @Test
    @DisplayName("Deve aplicar ordenação padrão quando sort contiver apenas espaços")
    void shouldUseDefaultSortWhenSortIsBlank() {
        // Arrange
        stubMaxPageSize();
        var expectedQuery = new ListBudgetsUseCase.ListBudgetsQuery(
                OWNER_ID, 1, 10, "createdAt", false);
        when(listBudgetsUseCase.list(expectedQuery))
                .thenReturn(new PageResult<>(List.of(), 1, 10, 0L));

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            var response = controller.list(1, 10, "   ");

            // Assert
            verify(listBudgetsUseCase).list(expectedQuery);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("Deve ordenar de forma ascendente quando apenas o campo for informado")
    void shouldSortAscendingWhenDirectionIsOmitted() {
        // Arrange
        stubMaxPageSize();
        var expectedQuery = new ListBudgetsUseCase.ListBudgetsQuery(
                OWNER_ID, 2, 10, "updatedAt", true);
        when(listBudgetsUseCase.list(expectedQuery))
                .thenReturn(new PageResult<>(List.of(), 2, 10, 0L));

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            var response = controller.list(2, 10, " updatedAt ");

            // Assert
            verify(listBudgetsUseCase).list(expectedQuery);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("Deve ordenar de forma descendente quando direção desc for informada")
    void shouldSortDescending() {
        // Arrange
        stubMaxPageSize();
        var expectedQuery = new ListBudgetsUseCase.ListBudgetsQuery(
                OWNER_ID, 1, 10, "periodStart", false);
        when(listBudgetsUseCase.list(expectedQuery))
                .thenReturn(new PageResult<>(List.of(), 1, 10, 0L));

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            var response = controller.list(1, 10, "periodStart, DESC ");

            // Assert
            verify(listBudgetsUseCase).list(expectedQuery);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("Deve aceitar direção asc explicitamente sem diferenciar maiúsculas de minúsculas")
    void shouldAcceptExplicitAscendingSort() {
        // Arrange
        stubMaxPageSize();
        var expectedQuery = new ListBudgetsUseCase.ListBudgetsQuery(
                OWNER_ID, 1, 10, "status", true);
        when(listBudgetsUseCase.list(expectedQuery))
                .thenReturn(new PageResult<>(List.of(), 1, 10, 0L));

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            var response = controller.list(1, 10, "status,ASC");

            // Assert
            verify(listBudgetsUseCase).list(expectedQuery);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
        }
    }

    @Test
    @DisplayName("Deve rejeitar página negativa")
    void shouldRejectNegativePage() {
        // Arrange
        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.list(-1, 10, "createdAt");

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(PaginationParameterInvalidException.class)
                    .hasMessage("Field 'page' must be greater than or equal to zero");

            verifyNoInteractions(listBudgetsUseCase);
        }
    }

    @Test
    @DisplayName("Deve rejeitar tamanho de página menor que um")
    void shouldRejectPageSizeBelowMinimum() {
        // Arrange
        stubMaxPageSize();

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.list(0, 0, "createdAt");

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(PaginationParameterInvalidException.class)
                    .hasMessage("Field 'size' must be between 1 and " + MAX_SIZE);

            verifyNoInteractions(listBudgetsUseCase);
        }
    }

    @Test
    @DisplayName("Deve rejeitar tamanho de página maior que o máximo permitido")
    void shouldRejectPageSizeAboveMaximum() {
        // Arrange
        stubMaxPageSize();

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.list(0, MAX_SIZE + 1, "createdAt");

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(PaginationParameterInvalidException.class)
                    .hasMessage("Field 'size' must be between 1 and " + MAX_SIZE);

            verifyNoInteractions(listBudgetsUseCase);
        }
    }

    @Test
    @DisplayName("Deve rejeitar campo de ordenação fora da lista permitida")
    void shouldRejectUnsupportedSortField() {
        // Arrange
        stubMaxPageSize();

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.list(0, 10, "amount,asc");

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(PaginationParameterInvalidException.class)
                    .hasMessageContaining("Field 'sort' must be one of:");

            verifyNoInteractions(listBudgetsUseCase);
        }
    }

    @Test
    @DisplayName("Deve rejeitar direção de ordenação diferente de asc ou desc")
    void shouldRejectUnsupportedSortDirection() {
        // Arrange
        stubMaxPageSize();

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.list(0, 10, "createdAt,sideways");

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(PaginationParameterInvalidException.class)
                    .hasMessage("Field 'sort' direction must be 'asc' or 'desc'");

            verifyNoInteractions(listBudgetsUseCase);
        }
    }

    @Test
    @DisplayName("Deve buscar orçamento quando ele pertencer ao usuário autenticado")
    void shouldGetOwnedBudget() {
        // Arrange
        BudgetResult budget = ownedBudget();
        BudgetResponse expectedBody = mock(BudgetResponse.class);
        when(getBudgetUseCase.get(BUDGET_ID)).thenReturn(budget);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner();
             MockedStatic<BudgetResponse> responseFactory = mockStatic(BudgetResponse.class)) {
            responseFactory.when(() -> BudgetResponse.from(budget)).thenReturn(expectedBody);

            // Act
            var response = controller.get(BUDGET_ID);

            // Assert
            verify(getBudgetUseCase).get(BUDGET_ID);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isSameAs(expectedBody);
        }
    }

    @Test
    @DisplayName("Deve negar acesso e incrementar métrica quando orçamento pertencer a outro usuário")
    void shouldRejectBudgetOwnedByAnotherUser() {
        // Arrange
        BudgetResult budget = mock(BudgetResult.class);
        Counter counter = mock(Counter.class);

        when(budget.userId()).thenReturn(OTHER_OWNER_ID);
        when(getBudgetUseCase.get(BUDGET_ID)).thenReturn(budget);
        when(meterRegistry.counter(
                "ecofy.budgeting.ownership.denied.total", "operation", "budget"))
                .thenReturn(counter);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner()) {
            // Act
            ThrowingCallable action = () -> controller.get(BUDGET_ID);

            // Assert
            assertThatThrownBy(action)
                    .isInstanceOf(BudgetAccessForbiddenException.class);

            verify(counter).increment();
        }
    }

    @Test
    @DisplayName("Deve retornar a visão geral dos orçamentos do usuário autenticado")
    void shouldReturnBudgetOverview() {
        // Arrange
        BudgetOverviewResponse expectedBody = mock(BudgetOverviewResponse.class);

        try (MockedStatic<AuthenticatedUser> authenticatedUser = mockAuthenticatedOwner();
             MockedStatic<BudgetOverviewResponse> responseFactory = mockStatic(BudgetOverviewResponse.class)) {
            responseFactory.when(() -> BudgetOverviewResponse.from(null)).thenReturn(expectedBody);

            // Act
            var response = controller.overview();

            // Assert
            verify(getBudgetOverviewUseCase).overview(OWNER_ID);
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isSameAs(expectedBody);
        }
    }

    private BudgetResult ownedBudget() {
        BudgetResult budget = mock(BudgetResult.class);
        when(budget.userId()).thenReturn(OWNER_ID);
        return budget;
    }

    private MockedStatic<AuthenticatedUser> mockAuthenticatedOwner() {
        when(props.security().ownerClaim()).thenReturn(OWNER_CLAIM);
        MockedStatic<AuthenticatedUser> authenticatedUser = mockStatic(AuthenticatedUser.class);
        authenticatedUser.when(() -> AuthenticatedUser.requireOwnerId(OWNER_CLAIM)).thenReturn(OWNER_ID);
        return authenticatedUser;
    }

    private void stubPaginationDefaults() {
        when(props.pagination().maxSize()).thenReturn(MAX_SIZE);
        when(props.pagination().defaultSize()).thenReturn(DEFAULT_SIZE);
    }

    private void stubMaxPageSize() {
        when(props.pagination().maxSize()).thenReturn(MAX_SIZE);
    }
}