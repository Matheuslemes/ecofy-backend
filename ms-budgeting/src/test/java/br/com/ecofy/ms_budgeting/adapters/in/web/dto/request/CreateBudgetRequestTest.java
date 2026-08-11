package br.com.ecofy.ms_budgeting.adapters.in.web.dto.request;

import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetPeriodType;
import br.com.ecofy.ms_budgeting.core.domain.enums.BudgetStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CreateBudgetRequestTest {

    private static final UUID USER_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID CATEGORY_ID =
            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private static final LocalDate PERIOD_START = LocalDate.of(2026, 6, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2026, 6, 30);

    // COMP-011: limite em centavos inteiros.
    private static final long LIMIT_CENTS = 100050L; // 1000.50

    private final Validator validator = validator();

    @Test
    void shouldCreateCreateBudgetRequestWithAllFields() {
        BudgetPeriodType periodType = anyBudgetPeriodType();
        BudgetStatus status = anyBudgetStatus();

        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID,
                CATEGORY_ID,
                periodType,
                PERIOD_START,
                PERIOD_END,
                LIMIT_CENTS,
                "BRL",
                status
        );

        assertEquals(USER_ID, request.userId());
        assertEquals(CATEGORY_ID, request.categoryId());
        assertEquals(periodType, request.periodType());
        assertEquals(PERIOD_START, request.periodStart());
        assertEquals(PERIOD_END, request.periodEnd());
        assertEquals(LIMIT_CENTS, request.limitAmountCents());
        assertEquals("BRL", request.currency());
        assertEquals(status, request.status());
    }

    @Test
    void shouldAcceptNullStatusBecauseStatusHasNoValidationAnnotation() {
        CreateBudgetRequest request = validRequest(null);

        Set<ConstraintViolation<CreateBudgetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
        assertNull(request.status());
    }

    @Test
    void shouldPassValidationWhenRequestIsValidWithMinimumLimitAmount() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID,
                CATEGORY_ID,
                anyBudgetPeriodType(),
                PERIOD_START,
                PERIOD_END,
                1L, // 1 centavo (mínimo)
                "BRL",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<CreateBudgetRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidationWhenRequiredFieldsAreNull() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<CreateBudgetRequest>> violations =
                validator.validate(request);

        Set<String> fields = propertyNames(violations);

        assertEquals(7, violations.size());
        assertTrue(fields.contains("userId"));
        assertTrue(fields.contains("categoryId"));
        assertTrue(fields.contains("periodType"));
        assertTrue(fields.contains("periodStart"));
        assertTrue(fields.contains("periodEnd"));
        assertTrue(fields.contains("limitAmountCents"));
        assertTrue(fields.contains("currency"));

        assertFalse(fields.contains("status"));
    }

    @Test
    void shouldFailValidationWhenLimitAmountIsLowerThanMinimum() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID,
                CATEGORY_ID,
                anyBudgetPeriodType(),
                PERIOD_START,
                PERIOD_END,
                0L,
                "BRL",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<CreateBudgetRequest>> violations =
                validator.validate(request);

        Set<String> fields = propertyNames(violations);

        assertEquals(1, violations.size());
        assertTrue(fields.contains("limitAmountCents"));
    }

    @Test
    void shouldFailValidationWhenCurrencyIsBlank() {
        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID,
                CATEGORY_ID,
                anyBudgetPeriodType(),
                PERIOD_START,
                PERIOD_END,
                10000L,
                "   ",
                anyBudgetStatus()
        );

        Set<ConstraintViolation<CreateBudgetRequest>> violations =
                validator.validate(request);

        Set<String> fields = propertyNames(violations);

        assertEquals(1, violations.size());
        assertTrue(fields.contains("currency"));
    }

    @Test
    void shouldCompareCreateBudgetRequestByAllRecordComponents() {
        BudgetPeriodType periodType = anyBudgetPeriodType();
        BudgetStatus status = anyBudgetStatus();

        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID, CATEGORY_ID, periodType, PERIOD_START, PERIOD_END, 50000L, "BRL", status);

        CreateBudgetRequest sameRequest = new CreateBudgetRequest(
                USER_ID, CATEGORY_ID, periodType, PERIOD_START, PERIOD_END, 50000L, "BRL", status);

        CreateBudgetRequest differentRequest = new CreateBudgetRequest(
                UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                CATEGORY_ID, periodType, PERIOD_START, PERIOD_END, 50000L, "BRL", status);

        assertEquals(request, request);
        assertEquals(request, sameRequest);
        assertNotEquals(request, differentRequest);
        assertNotEquals(request, null);
        assertNotEquals(request, "not-a-create-budget-request");
    }

    @Test
    void shouldGenerateHashCodeUsingAllRecordComponents() {
        BudgetPeriodType periodType = anyBudgetPeriodType();
        BudgetStatus status = anyBudgetStatus();

        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID, CATEGORY_ID, periodType, PERIOD_START, PERIOD_END, 50000L, "BRL", status);

        CreateBudgetRequest sameRequest = new CreateBudgetRequest(
                USER_ID, CATEGORY_ID, periodType, PERIOD_START, PERIOD_END, 50000L, "BRL", status);

        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());
    }

    @Test
    void shouldReturnToStringWithRecordComponents() {
        BudgetPeriodType periodType = anyBudgetPeriodType();
        BudgetStatus status = anyBudgetStatus();

        CreateBudgetRequest request = new CreateBudgetRequest(
                USER_ID, CATEGORY_ID, periodType, PERIOD_START, PERIOD_END, 50000L, "BRL", status);

        String result = request.toString();

        assertTrue(result.contains("CreateBudgetRequest"));
        assertTrue(result.contains("userId=" + USER_ID));
        assertTrue(result.contains("categoryId=" + CATEGORY_ID));
        assertTrue(result.contains("periodType=" + periodType));
        assertTrue(result.contains("periodStart=" + PERIOD_START));
        assertTrue(result.contains("periodEnd=" + PERIOD_END));
        assertTrue(result.contains("limitAmountCents=50000"));
        assertTrue(result.contains("currency=BRL"));
        assertTrue(result.contains("status=" + status));
    }

    private static CreateBudgetRequest validRequest(BudgetStatus status) {
        return new CreateBudgetRequest(
                USER_ID,
                CATEGORY_ID,
                anyBudgetPeriodType(),
                PERIOD_START,
                PERIOD_END,
                10000L,
                "BRL",
                status
        );
    }

    private static Set<String> propertyNames(
            Set<ConstraintViolation<CreateBudgetRequest>> violations
    ) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static Validator validator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    private static BudgetPeriodType anyBudgetPeriodType() {
        BudgetPeriodType[] values = BudgetPeriodType.values();

        if (values.length == 0) {
            throw new IllegalStateException("BudgetPeriodType enum must have at least one value");
        }

        return values[0];
    }

    private static BudgetStatus anyBudgetStatus() {
        BudgetStatus[] values = BudgetStatus.values();

        if (values.length == 0) {
            throw new IllegalStateException("BudgetStatus enum must have at least one value");
        }

        return values[0];
    }
}
