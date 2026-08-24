# Project: RAG Legislation Assistant

A Retrieval-Augmented Generation chatbot answering questions about public norms, built with Spring AI + Claude (Anthropic) + pgvector.

## Stack

- **Backend**: Java 21, Spring Boot 3.3, Spring AI 1.0.0
- **Chat model**: Claude via `spring-ai-starter-model-anthropic` (requires `ANTHROPIC_API_KEY` env var)
- **Embeddings**: local ONNX model (`TransformersEmbeddingModel`, all-MiniLM-L6-v2, 384 dimensions) — no API key needed, runs on-machine
- **Vector store**: PostgreSQL + pgvector extension via `spring-ai-starter-vector-store-pgvector`
- **Frontend**: React 18, TypeScript, Vite

## Project Structure

- `backend/src/main/java/.../controller/` — ChatController (POST /api/chat), IngestController (POST /api/ingest)
- `backend/src/main/java/.../service/ChatService.java` — the RAG pipeline: retrieval (vectorStore.similaritySearch) + augmentation (prompt template) + generation (ChatClient call to Claude)
- `backend/src/main/java/.../service/IngestionService.java` — loads .txt files from `sample-docs/`, splits via TokenTextSplitter, stores in VectorStore
- `backend/src/main/java/.../config/EmbeddingConfig.java` — explicit bean for the local embedding model
- `backend/src/main/resources/sample-docs/` — sample documents to ingest (plain .txt files)
- `frontend/src/App.tsx` — simple chat UI with an "ingest" button and source-citation display

## Commands

```bash
# Backend (needs ANTHROPIC_API_KEY set, and Postgres+pgvector running)
cd backend && mvn spring-boot:run

# Full stack via Docker
export ANTHROPIC_API_KEY=your-key
docker compose up --build

# Frontend
cd frontend && npm run dev

# Tests
cd backend && mvn test
```

## Conventions

- All code, comments, and commit messages in English.
- Keep the RAG system prompt (in ChatService) strict about not answering outside the retrieved context — this is the core "trust" feature of the project, don't loosen it casually.
- New ingested document types should go in `sample-docs/` as plain `.txt` for now; PDF support is a roadmap item.

## Known Follow-ups (see README Roadmap)

- No integration test yet exercises the real vector store + real embedding model end-to-end; a Testcontainers-based test using the `pgvector/pgvector` image is the natural next step.
- Ingestion doesn't deduplicate; calling `/api/ingest` multiple times will duplicate chunks. Fine for a demo, not for production.
- No streaming (SSE) yet — `ChatService.answer()` blocks until Claude returns the full response.

## Current Status

Working local implementation: ingestion, retrieval, and RAG-grounded chat all functional via Docker Compose. Not yet deployed. Uses the sample "trabalho remoto" (remote work) norm as the only ingested document so far — swap or add files in `sample-docs/` to change the domain.
