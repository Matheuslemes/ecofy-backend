package br.com.ecofy.ms_insights.adapters.in.web.advice;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(

        Instant timestamp,

        int status,

        String errorCode,

        String message,

        String path,

        String traceId,

        Map<String, Object> details

) { }
