import { useState } from "react";
import { askQuestion, ingestDocuments, type ChatResponse } from "./services/api";

interface Message {
  role: "user" | "assistant";
  content: string;
  sources?: ChatResponse["sources"];
}

function App() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [ingesting, setIngesting] = useState(false);
  const [ingestStatus, setIngestStatus] = useState<string | null>(null);

  async function handleIngest() {
    setIngesting(true);
    setIngestStatus(null);
    try {
      const result = await ingestDocuments();
      setIngestStatus(
        `${result.documentsIngested} documento(s), ${result.chunksCreated} trecho(s) indexados.`
      );
    } catch (err) {
      setIngestStatus("Erro ao indexar documentos. Veja o console do backend.");
    } finally {
      setIngesting(false);
    }
  }

  async function handleSend() {
    if (!input.trim()) return;
    const question = input;
    setInput("");
    setMessages((prev) => [...prev, { role: "user", content: question }]);
    setLoading(true);
    try {
      const response = await askQuestion(question);
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: response.answer, sources: response.sources },
      ]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: "Erro ao consultar o assistente. Verifique se o backend está rodando." },
      ]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <main style={{ fontFamily: "sans-serif", padding: "2rem", maxWidth: 720, margin: "0 auto" }}>
      <h1>Assistente de Legislação (RAG)</h1>
      <p style={{ color: "#555" }}>
        Faça perguntas sobre as normas indexadas. As respostas são geradas pelo Claude,
        com base apenas nos trechos recuperados dos documentos.
      </p>

      <div style={{ marginBottom: "1.5rem" }}>
        <button onClick={handleIngest} disabled={ingesting}>
          {ingesting ? "Indexando..." : "1. Indexar documentos de exemplo"}
        </button>
        {ingestStatus && <p style={{ fontSize: "0.9rem", color: "#555" }}>{ingestStatus}</p>}
      </div>

      <div style={{ border: "1px solid #ddd", borderRadius: 8, padding: "1rem", minHeight: 200, marginBottom: "1rem" }}>
        {messages.length === 0 && (
          <p style={{ color: "#999" }}>Nenhuma pergunta ainda. Indexe os documentos e pergunte algo como: "Quais os requisitos para trabalho remoto?"</p>
        )}
        {messages.map((msg, i) => (
          <div key={i} style={{ marginBottom: "1rem" }}>
            <strong>{msg.role === "user" ? "Você" : "Assistente"}:</strong>
            <p style={{ whiteSpace: "pre-wrap" }}>{msg.content}</p>
            {msg.sources && msg.sources.length > 0 && (
              <details style={{ fontSize: "0.85rem", color: "#666" }}>
                <summary>Fontes consultadas ({msg.sources.length})</summary>
                {msg.sources.map((s, j) => (
                  <p key={j}>
                    <em>{s.documentName}:</em> {s.excerpt}
                  </p>
                ))}
              </details>
            )}
          </div>
        ))}
      </div>

      <div style={{ display: "flex", gap: "0.5rem" }}>
        <input
          style={{ flex: 1, padding: "0.5rem" }}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSend()}
          placeholder="2. Digite sua pergunta..."
        />
        <button onClick={handleSend} disabled={loading}>
          {loading ? "Pensando..." : "Perguntar"}
        </button>
      </div>
    </main>
  );
}

export default App;
