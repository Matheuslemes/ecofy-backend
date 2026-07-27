package br.com.ecofy.ms_budgeting.adapters.in.web.dto.response;

import br.com.ecofy.ms_budgeting.core.application.result.BudgetConsumptionResult;
import br.com.ecofy.ms_budgeting.core.application.result.BudgetOverviewResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class BudgetOverviewResponseTest {

    private static final UUID USER_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID BUDGET_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static List<BudgetConsumptionResponse> consumptions() {
        return List.of(new BudgetConsumptionResponse(BUDGET_ID, 5000L, 10000L, new BigDecimal("50.0")));
    }

    @Test
    void shouldCreateBudgetOverviewResponseWithAllFields() {
        List<BudgetConsumptionResponse> consumptions = consumptions();
        List<?> alerts = List.of("alert-001", "alert-002");

        BudgetOverviewResponse response = new BudgetOverviewResponse(
                USER_ID,
                consumptions,
                alerts
        );

        assertEquals(USER_ID, response.userId());
        assertEquals(consumptions, response.consumptions());
        assertEquals(alerts, response.alerts());
    }

    @Test
    void shouldCreateBudgetOverviewResponseWithNullFields() {
        BudgetOverviewResponse response = new BudgetOverviewResponse(
                null,
                null,
                null
        );

        assertNull(response.userId());
        assertNull(response.consumptions());
        assertNull(response.alerts());
    }

    @Test
    void shouldMapConsumptionsToCentsFromBudgetOverviewResult() {
        // COMP-011: o overview mapeia BudgetConsumptionResult (BigDecimal) -> centavos.
        List<BudgetConsumptionResult> resultConsumptions = List.of(
                new BudgetConsumptionResult(BUDGET_ID, new BigDecimal("50.00"), new BigDecimal("100.00"), new BigDecimal("50.0"))
        );
        List<?> alerts = List.of("alert-001", "alert-002");

        BudgetOverviewResult result = mock(BudgetOverviewResult.class);
        doReturn(USER_ID).when(result).userId();
        doReturn(resultConsumptions).when(result).consumptions();
        doReturn(alerts).when(result).alerts();

        BudgetOverviewResponse response = BudgetOverviewResponse.from(result);

        assertEquals(USER_ID, response.userId());
        assertSame(alerts, response.alerts());
        assertEquals(1, response.consumptions().size());

        BudgetConsumptionResponse c = response.consumptions().get(0);
        assertEquals(BUDGET_ID, c.budgetId());
        assertEquals(5000L, c.consumedCents());
        assertEquals(10000L, c.limitCents());
        assertEquals(new BigDecimal("50.0"), c.consumedPct());
    }

    @Test
    void shouldThrowNullPointerExceptionWhenFromReceivesNullResult() {
        assertThrows(
                NullPointerException.class,
                () -> BudgetOverviewResponse.from(null)
        );
    }

    @Test
    void shouldCompareBudgetOverviewResponseByAllRecordComponents() {
        List<BudgetConsumptionResponse> consumptions = consumptions();
        List<?> alerts = List.of("alert-001");

        BudgetOverviewResponse response = new BudgetOverviewResponse(USER_ID, consumptions, alerts);
        BudgetOverviewResponse sameResponse = new BudgetOverviewResponse(USER_ID, consumptions, alerts);
        BudgetOverviewResponse differentResponse = new BudgetOverviewResponse(
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), consumptions, alerts);

        assertEquals(response, response);
        assertEquals(response, sameResponse);
        assertNotEquals(response, differentResponse);
        assertNotEquals(response, null);
        assertNotEquals(response, "not-a-budget-overview-response");
    }

    @Test
    void shouldGenerateHashCodeUsingAllRecordComponents() {
        List<BudgetConsumptionResponse> consumptions = consumptions();
        List<?> alerts = List.of("alert-001");

        BudgetOverviewResponse response = new BudgetOverviewResponse(USER_ID, consumptions, alerts);
        BudgetOverviewResponse sameResponse = new BudgetOverviewResponse(USER_ID, consumptions, alerts);

        assertEquals(response, sameResponse);
        assertEquals(response.hashCode(), sameResponse.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenAlertsChange() {
        List<BudgetConsumptionResponse> consumptions = consumptions();

        BudgetOverviewResponse response = new BudgetOverviewResponse(USER_ID, consumptions, List.of("alert-001"));
        BudgetOverviewResponse differentResponse = new BudgetOverviewResponse(USER_ID, consumptions, List.of("alert-002"));

        assertNotEquals(response, differentResponse);
    }

    @Test
    void shouldReturnToStringWithRecordComponents() {
        List<BudgetConsumptionResponse> consumptions = consumptions();
        List<?> alerts = List.of("alert-001");

        BudgetOverviewResponse response = new BudgetOverviewResponse(USER_ID, consumptions, alerts);

        String result = response.toString();

        assertTrue(result.contains("BudgetOverviewResponse"));
        assertTrue(result.contains("userId=" + USER_ID));
        assertTrue(result.contains("consumptions="));
        assertTrue(result.contains("alerts=" + alerts));
    }
}
