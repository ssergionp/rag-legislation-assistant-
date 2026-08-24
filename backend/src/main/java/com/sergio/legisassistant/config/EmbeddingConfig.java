package com.sergio.legisassistant.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The pgvector store needs an EmbeddingModel to turn text into vectors.
 * We use a local ONNX model (all-MiniLM-L6-v2, 384 dimensions) instead of
 * a paid embeddings API — it runs on-machine, has no per-call cost, and is
 * good enough for a portfolio-scale RAG demo. Only the chat model (Claude)
 * calls out to a paid API in this project.
 *
 * NOTE: the model files are downloaded automatically on first use and
 * cached locally; the first request after startup will be slower.
 */
@Configuration
public class EmbeddingConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new TransformersEmbeddingModel();
    }
}
