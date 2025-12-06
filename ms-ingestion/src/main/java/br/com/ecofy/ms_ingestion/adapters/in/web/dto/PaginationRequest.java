package br.com.ecofy.ms_ingestion.adapters.in.web.dto;

public record PaginationRequest(

        int page,
        int size

) { }