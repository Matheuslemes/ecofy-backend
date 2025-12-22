package br.com.ecofy.ms_categorization.adapters.in.web.advice;

import br.com.ecofy.ms_categorization.core.application.exception.BusinessValidationException;
import br.com.ecofy.ms_categorization.core.application.exception.CategoryNotFoundException;
import br.com.ecofy.ms_categorization.core.application.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleCategory(CategoryNotFoundException ex) {
        return new ApiErrorResponse("CAT-404", ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleTx(TransactionNotFoundException ex) {
        return new ApiErrorResponse("TX-404", ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(BusinessValidationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiErrorResponse handleBusiness(BusinessValidationException ex) {
        return new ApiErrorResponse("BUS-422", ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleGeneric(Exception ex) {
        return new ApiErrorResponse("GEN-500", "Unexpected error", Instant.now());
    }

}