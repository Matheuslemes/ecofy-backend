package br.com.ecofy.ms_budgeting.adapters.in.web.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyCents {

    private MoneyCents() {
    }

    // Converte um valor monetário decimal em centavos inteiros (ex.: 123.45 -> 12345).
    public static long toCents(BigDecimal amount) {
        return amount
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    // Converte centavos inteiros em valor monetário decimal (ex.: 12345 -> 123.45).
    public static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents).movePointLeft(2);
    }
}
