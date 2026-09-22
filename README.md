# java-chat-example

**Spring Boot Agentic RAG Application**

This project is a production-ready, agentic AI chat backend that combines conversational memory, tool execution,
and Retrieval-Augmented Generation (RAG).
Built on Spring Boot 3 and Spring AI, it streams responses via Server-Sent Events (SSE)
using a decoupled orchestrator architecture. It leverages a dual-model approach—routing chat generation to a
cloud-hosted LLM and embedding generation to a local model — while storing vectorized document chunks in a cloud
PostgreSQL database
using pgvector.The application supports dynamic tool injection, similarity-thresholded context retrieval,
and strict infrastructure-as-code practices via Flyway migrations.

1. Foundation: Spring Boot REST API, Swagger UI, and SSE streaming.
2. Persistence: H2 in-memory → H2 file → Aiven Cloud PostgreSQL with Flyway migrations.
3. AI Integration: Manual Ollama Cloud configuration (Bearer auth + dual HTTP clients for sync/stream).
4. Memory: Conversation history loading, prompt assembly, and token-based history limiting.
5. Streaming Architecture: Extracted SSE logic out of the controller into an orchestrator (`JpaChatService`) and stream
   sinks.
6. Agentic Core: Abstracted tool calling via a dynamic `List<AgentTool>` injection pattern.
7. RAG Infrastructure: Dual-Ollama setup (Cloud LLM for chat, Local `mxbai-embed-large` for embeddings) + pgvector.
8. RAG Pipeline: Document chunking, ingestion, distance-to-similarity conversion, threshold filtering, and metadata
   extraction.
9. Clean Architecture: Extracted retrieval logic into `DocumentRetrievalService`.
10. Augmentation: Connected the RAG pipeline to the Chat pipeline so the AI answers using your private knowledge base.
11. TODO .... and I can continue here many things, like guardrails, etc.

## 1. Prerequisites

#### 1.a) Ollama chat model

In this example I use Ollama cloud model. Set your **OLLAMA_API_KEY** in the OS.

#### 1.b) Postgres DB instance

At some point I use Postgres DB at aiven.io.

My console: https://console.aiven.io/account/a5e0f2593af3/project/java-chat-example/services

You should set the followings (in OS or in the .env file):

- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD

You might need **pgvector**. To install it, run in the DB Studio:

```
CREATE EXTENSION IF NOT EXISTS vector;
```

#### 1.c) Embedding model

I use Ollama's **mxbai-embed-large** local embedding.

```shell
ollama pull mxbai-embed-large:latest
```

## 2. How to run

Install maven dependencies with ```mvn clean install```

Then run the application: ```mvn clean spring-boot:run```

### Swagger

http://localhost:8080/swagger-ui/index.html

### H2 console

If you use H2, the admin console is: http://localhost:8080/h2-console

## 3. Test

```shell
curl -N -X POST http://localhost:8080/api/v1/chat/stream -H "Content-Type: application/json" -d "{\"message\":\"Write a short poem about Java virtual threads\"}"
```

Check what extensions are installed in your database.

```postgres-sql
SELECT * FROM pg_extension;
```
