package br.com.ecofy.ms_categorization.core.domain.valueobject;

import java.util.Objects;

public final class Merchant {

    private final String raw;
    private final String normalized;

    public Merchant(String raw, String normalized) {
        this.raw = Objects.requireNonNull(raw, "raw must not be null");
        this.normalized = Objects.requireNonNull(normalized, "normalized must not be null");
    }

    public String getRaw() {
        return raw;
    }

    public String getNormalized() {
        return normalized;
    }

    public static Merchant of(String description) {
        var raw = description == null ? "" : description;
        var normalized = Normalizer.normalize(raw);
        return new Merchant(raw, normalized);
    }

    static final class Normalizer {
        private Normalizer() {}

        static String normalize(String s) {
            if (s == null) return "";
            String t = s.trim().toLowerCase();
            t = java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            t = t.replaceAll("[^a-z0-9\\s]", " ");
            t = t.replaceAll("\\s+", " ").trim();
            return t;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Merchant merchant)) return false;
        return raw.equals(merchant.raw) &&
                normalized.equals(merchant.normalized);
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, normalized);
    }

    @Override
    public String toString() {
        return "Merchant[" +
                "raw=" + raw +
                ", normalized=" + normalized +
                ']';
    }
}
