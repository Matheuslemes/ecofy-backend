package br.com.ecofy.ms_notification.core.domain;


import java.util.Map;

final class SimpleTemplateEngine {

    private SimpleTemplateEngine() {}

    /**
     * Placeholder "senior": simples e seguro.
     * Substitui {{key}} por valor (toString), sem executar código.
     */
    static String render(String template, Map<String, Object> vars) {
        if (template == null) return null;
        String out = template;
        if (vars == null || vars.isEmpty()) return out;

        for (var e : vars.entrySet()) {
            String key = "{{" + e.getKey() + "}}";
            String val = e.getValue() == null ? "" : String.valueOf(e.getValue());
            out = out.replace(key, val);
        }
        return out;
    }
}