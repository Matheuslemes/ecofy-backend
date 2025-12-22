package br.com.ecofy.ms_categorization.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(

        @NotBlank
        String name,

        String color

) { }