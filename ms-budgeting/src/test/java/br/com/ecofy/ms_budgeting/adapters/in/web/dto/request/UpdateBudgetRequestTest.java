package br.com.ecofy.ms_budgeting.adapters.in.web.dto.request;

import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UpdateBudgetRequestTest {

    private final Validator validator = validator();

    @Test
    void shouldCreateUpdateBudgetRequestWithAllFields() {
        Long newLimitAmountCents = 50000L; // 500.00
        BudgetStatus status = anyBudgetStatus();

        UpdateBudgetRequest request = new UpdateBudgetRequest(
                newLimitAmountCents,
                "BRL",
                status
        );

        assertEquals(newLimitAmountCents, request.newLimitAmountCents());
        assertEquals("BRL", request.currency());
        assertEquals(status, request.status());
    }

    @Test
    void shouldCreateUpdateBudgetRequestWithNullFields() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                null,
                null,
                null
        );

        assertNull(request.newLimitAmountCents());
        assertNull(request.currency());
        assertNull(request.status());
    }

    @Test
    void shouldPassValidationWhenAllFieldsAreNullBecauseOnlyMinExists() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                null,
                null,
                null
        );

        Set<ConstraintViolation<UpdateBudgetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenNewLimitAmountIsEqualToMinimum() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                1L, // 1 centavo (mínimo)
                "BRL",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<UpdateBudgetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldPassValidationWhenNewLimitAmountIsGreaterThanMinimum() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                10000L,
                "BRL",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<UpdateBudgetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenNewLimitAmountIsLowerThanMinimum() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                0L,
                "BRL",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<UpdateBudgetRequest>> violations =
                validator.validate(request);

        Set<String> fields = propertyNames(violations);

        assertEquals(1, violations.size());
        assertTrue(fields.contains("newLimitAmountCents"));
    }

    @Test
    void shouldFailValidationWhenNewLimitAmountIsNegative() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                -1000L,
                "BRL",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<UpdateBudgetRequest>> violations =
                validator.validate(request);

        Set<String> fields = propertyNames(violations);

        assertEquals(1, violations.size());
        assertTrue(fields.contains("newLimitAmountCents"));
    }

    @Test
    void shouldAllowBlankCurrencyBecauseCurrencyHasNoValidationAnnotation() {
        UpdateBudgetRequest request = new UpdateBudgetRequest(
                10000L,
                "   ",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<UpdateBudgetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals("   ", request.currency());
    }

    @Test
    void shouldCompareUpdateBudgetRequestByAllRecordComponents() {
        BudgetStatus status = anyBudgetStatus();

        UpdateBudgetRequest request = new UpdateBudgetRequest(10000L, "BRL", status);
        UpdateBudgetRequest sameRequest = new UpdateBudgetRequest(10000L, "BRL", status);
        UpdateBudgetRequest differentRequest = new UpdateBudgetRequest(20000L, "BRL", status);

        assertEquals(request, request);
        assertEquals(request, sameRequest);
        assertNotEquals(request, differentRequest);
        assertNotEquals(request, null);
        assertNotEquals(request, "not-an-update-budget-request");
    }

    @Test
    void shouldGenerateHashCodeUsingAllRecordComponents() {
        BudgetStatus status = anyBudgetStatus();

        UpdateBudgetRequest request = new UpdateBudgetRequest(10000L, "BRL", status);
        UpdateBudgetRequest sameRequest = new UpdateBudgetRequest(10000L, "BRL", status);

        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenCurrencyChanges() {
        BudgetStatus status = anyBudgetStatus();

        UpdateBudgetRequest request = new UpdateBudgetRequest(10000L, "BRL", status);
        UpdateBudgetRequest differentRequest = new UpdateBudgetRequest(10000L, "USD", status);

        assertNotEquals(request, differentRequest);
    }

    @Test
    void shouldNotBeEqualWhenStatusChanges() {
        BudgetStatus[] statuses = BudgetStatus.values();

        if (statuses.length < 2) {
            return;
        }

        UpdateBudgetRequest request = new UpdateBudgetRequest(10000L, "BRL", statuses[0]);
        UpdateBudgetRequest differentRequest = new UpdateBudgetRequest(10000L, "BRL", statuses[1]);

        assertNotEquals(request, differentRequest);
    }

    @Test
    void shouldReturnToStringWithRecordComponents() {
        BudgetStatus status = anyBudgetStatus();

        UpdateBudgetRequest request = new UpdateBudgetRequest(10000L, "BRL", status);

        String result = request.toString();

        assertTrue(result.contains("UpdateBudgetRequest"));
        assertTrue(result.contains("newLimitAmountCents=10000"));
        assertTrue(result.contains("currency=BRL"));
        assertTrue(result.contains("status=" + status));
    }

    private static Set<String> propertyNames(
            Set<ConstraintViolation<UpdateBudgetRequest>> violations
    ) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    private static BudgetStatus anyBudgetStatus() {
        BudgetStatus[] values = BudgetStatus.values();

        if (values.length == 0) {
            throw new IllegalStateException("BudgetStatus enum must have at least one value");
        }

        return values[0];
    }
}
