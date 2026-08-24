package com.sergio.legisassistant.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads plain-text documents from src/main/resources/sample-docs, splits them
 * into overlapping chunks, embeds each chunk (via the configured local
 * EmbeddingModel), and stores them in the pgvector-backed VectorStore.
 *
 * This is a deliberately simple ingestion pipeline for a portfolio project:
 * real-world RAG systems would add deduplication, incremental re-ingestion,
 * metadata-rich chunking (by section/article), and probably a proper file
 * upload endpoint instead of a fixed classpath folder.
 */
@Service
public class IngestionService {

    private static final String DOCS_LOCATION = "classpath:sample-docs/*.txt";

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * @return number of source documents processed and number of chunks stored
     */
    public IngestionResult ingestSampleDocuments() throws java.io.IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(DOCS_LOCATION);

        List<Document> allChunks = new ArrayList<>();

        for (Resource resource : resources) {
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("source", resource.getFilename());
            List<Document> rawDocs = reader.get();
            List<Document> chunks = textSplitter.apply(rawDocs);
            allChunks.addAll(chunks);
        }

        if (!allChunks.isEmpty()) {
            vectorStore.add(allChunks);
        }

        return new IngestionResult(resources.length, allChunks.size());
    }

    public record IngestionResult(int documentsIngested, int chunksCreated) {
    }
}
