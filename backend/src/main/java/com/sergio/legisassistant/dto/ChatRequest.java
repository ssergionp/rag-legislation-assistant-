package com.sergio.legisassistant.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ChatRequest(
        @NotBlank String question
) {
}
