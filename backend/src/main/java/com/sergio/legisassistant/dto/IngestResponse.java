package com.sergio.legisassistant.dto;

public record IngestResponse(
        int documentsIngested,
        int chunksCreated
) {
}
