import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8081/api";

export interface SourceExcerpt {
  documentName: string;
  excerpt: string;
}

export interface ChatResponse {
  answer: string;
  sources: SourceExcerpt[];
}

export async function askQuestion(question: string): Promise<ChatResponse> {
  const { data } = await axios.post<ChatResponse>(`${API_BASE_URL}/chat`, { question });
  return data;
}

export async function ingestDocuments(): Promise<{ documentsIngested: number; chunksCreated: number }> {
  const { data } = await axios.post(`${API_BASE_URL}/ingest`);
  return data;
}
