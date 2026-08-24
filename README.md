# RAG Legislation Assistant

A Retrieval-Augmented Generation (RAG) chatbot that answers questions about public norms and regulations, grounded strictly in the ingested documents — built with Spring AI, Claude (Anthropic), and pgvector.

![CI](https://github.com/YOUR_USERNAME/rag-legislation-assistant/actions/workflows/ci.yml/badge.svg)

## Why this project

Generative AI integration is the most in-demand skill in software today, but "calling the OpenAI/Anthropic API" isn't the hard part — grounding answers in real documents so the model doesn't hallucinate is. This project demonstrates a full RAG pipeline:

1. **Ingestion** — plain-text norms are split into overlapping chunks and embedded into vectors using a local ONNX model (no embedding API costs).
2. **Retrieval** — a user's question is embedded and matched against the stored chunks using vector similarity search (pgvector, cosine distance).
3. **Augmentation** — the most relevant chunks are injected into the prompt as context.
4. **Generation** — Claude answers using *only* that context, explicitly refusing to guess when the documents don't cover the question.

## Tech Stack

| Layer          | Technology                                   |
|----------------|-----------------------------------------------|
| Backend        | Java 21, Spring Boot 3, Spring AI              |
| Chat model     | Claude (Anthropic API) via Spring AI           |
| Embeddings     | Local ONNX model (all-MiniLM-L6-v2), no API cost |
| Vector store   | PostgreSQL + pgvector extension                |
| Frontend       | React, TypeScript, Vite                        |
| DevOps         | Docker, Docker Compose, GitHub Actions         |

## How RAG grounding works here

Every question triggers a similarity search against the vector store *before* the model ever sees it (see `ChatService`). The system prompt explicitly instructs Claude to answer only from the retrieved context and to say so plainly when the context is insufficient — this is what separates a "chatbot with a system prompt" from an actual RAG system: the model literally cannot answer from parts of its training data that contradict or go beyond the documents you gave it.

## Getting Started

### Prerequisites

- An [Anthropic API key](https://console.anthropic.com/) (Claude)
- Docker (for PostgreSQL + pgvector)

### Run with Docker Compose

```bash
export ANTHROPIC_API_KEY=your-key-here   # Windows: set ANTHROPIC_API_KEY=your-key-here
docker compose up --build
```

Backend available at `http://localhost:8081`.

### Run the frontend

```bash
cd frontend
npm install
npm run dev
```

App available at `http://localhost:5173`.

### Using the app

1. Click **"Indexar documentos de exemplo"** once — this ingests the sample norm (`backend/src/main/resources/sample-docs/norma-trabalho-remoto.txt`) into the vector store.
2. Ask a question in Portuguese or English, e.g. *"Quais os requisitos para trabalho remoto?"*
3. Expand **"Fontes consultadas"** under the answer to see which chunks were retrieved and used.

### Run tests

```bash
cd backend
mvn test
```

## Design Notes

- **Local embeddings, cloud chat model**: embedding every chunk through a paid API adds cost and latency for no real benefit at this scale, so embeddings run locally via ONNX. Only the final answer generation calls out to Claude — this is a common and cost-effective pattern in production RAG systems too.
- **Strict grounding over general knowledge**: the system prompt explicitly forbids answering from outside the retrieved context. This trades some flexibility (the model won't fill gaps with plausible-sounding general knowledge) for trustworthiness, which matters far more for a document Q&A system over official norms.
- **Fixed sample documents over file upload**: keeps the demo simple and reproducible. A real deployment would add a document upload endpoint — see Roadmap.

## Roadmap

- [ ] Add a file upload endpoint for ingesting arbitrary documents (PDF/text)
- [ ] Stream responses via SSE for a more responsive chat UI
- [ ] Add integration tests with Testcontainers (pgvector image)
- [ ] Deploy backend (Render/Fly.io) and frontend (Vercel)
- [ ] Add source-chunk highlighting in the UI (show exactly which sentence supported the answer)

## License

MIT
