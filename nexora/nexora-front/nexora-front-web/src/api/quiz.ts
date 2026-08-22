export interface QuizQuestion {
  type: string;
  question: string;
  options: string[];
  /** 正确选项下标（0 开始） */
  answer: number;
  analysis?: string;
}

export interface QuizScript {
  title: string;
  questions: QuizQuestion[];
}

export function parseQuizScript(content?: string): QuizScript | null {
  if (!content) {
    return null;
  }
  try {
    const parsed = JSON.parse(content);
    if (!parsed || typeof parsed !== 'object' || !Array.isArray(parsed.questions)) {
      return null;
    }
    return parsed as QuizScript;
  } catch {
    return null;
  }
}