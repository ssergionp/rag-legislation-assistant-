package com.sergio.legisassistant.dto;

import java.util.List;

public record ChatResponse(
        String answer,
        List<SourceExcerpt> sources
) {
    public record SourceExcerpt(
            String documentName,
            String excerpt
    ) {
    }
}
