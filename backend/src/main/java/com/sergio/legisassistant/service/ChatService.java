package com.sergio.legisassistant.service;

import com.sergio.legisassistant.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements the classic RAG (Retrieval-Augmented Generation) pattern:
 *
 * 1. Embed the user's question and search the VectorStore for the most
 *    semantically similar document chunks (retrieval).
 * 2. Inject those chunks into the prompt as context (augmentation).
 * 3. Ask the chat model (Claude) to answer using ONLY that context
 *    (generation), so answers stay grounded in the ingested documents
 *    instead of the model's general training knowledge.
 */
@Service
public class ChatService {

    private static final String SYSTEM_TEMPLATE = """
            You are an assistant that answers questions strictly based on the
            provided context, which comes from official public norms and
            regulations. Follow these rules:

            - Answer only using the information in the context below.
            - If the context doesn't contain enough information to answer,
              say so clearly instead of guessing or using outside knowledge.
            - Always answer in the same language the question was asked in.
            - Be precise and cite which part of the context supports your answer.

            Context:
            ---------------------
            %s
            ---------------------
            """;

    private static final int TOP_K = 4;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public ChatResponse answer(String question) {
        List<Document> relevantChunks = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(TOP_K).build());

        String context = relevantChunks.isEmpty()
                ? "(no relevant documents found)"
                : relevantChunks.stream()
                    .map(Document::getText)
                    .reduce("", (a, b) -> a + "\n\n" + b);

        String systemPrompt = SYSTEM_TEMPLATE.formatted(context);

        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call()
                .content();

        List<ChatResponse.SourceExcerpt> sources = relevantChunks.stream()
                .map(doc -> new ChatResponse.SourceExcerpt(
                        String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")),
                        truncate(doc.getText(), 200)
                ))
                .toList();

        return new ChatResponse(answer, sources);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}
