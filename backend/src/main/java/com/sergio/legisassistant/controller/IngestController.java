package com.sergio.legisassistant.controller;

import com.sergio.legisassistant.dto.IngestResponse;
import com.sergio.legisassistant.service.IngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private final IngestionService ingestionService;

    public IngestController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Loads every .txt file in src/main/resources/sample-docs into the
     * vector store. Safe to call multiple times while iterating locally;
     * note it does NOT deduplicate, so repeated calls will duplicate
     * chunks — see IngestionService's class docs for real-world caveats.
     */
    @PostMapping
    public ResponseEntity<IngestResponse> ingest() throws java.io.IOException {
        IngestionService.IngestionResult result = ingestionService.ingestSampleDocuments();
        return ResponseEntity.ok(new IngestResponse(result.documentsIngested(), result.chunksCreated()));
    }
}
