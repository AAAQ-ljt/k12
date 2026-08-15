import { QUESTION_TYPE_MAP } from '@/types/common';

interface ExportQuestion {
  title: string;
  questionType: number;
  score: number;
}

interface ExportGroup {
  groupName: string;
  questions: ExportQuestion[];
}

export function buildPaperMarkdown(
  paperName: string,
  grade: string | undefined,
  groups: ExportGroup[],
): string {
  const totalScore = groups.reduce(
    (sum, group) => sum + group.questions.reduce((s, q) => s + (q.score || 0), 0),
    0,
  );
  const lines: string[] = [`# ${paperName}`, '', `年级：${grade ?? ''}`, `总分：${totalScore}`, ''];
  groups.forEach((group, groupIndex) => {
    lines.push(`## ${groupIndex + 1}、${group.groupName}`, '');
    group.questions.forEach((q, index) => {
      const typeText = QUESTION_TYPE_MAP[String(q.questionType)]?.text ?? '';
      lines.push(`${index + 1}. ${q.title}（${typeText}，${q.score}分）`, '');
    });
  });
  return lines.join('\n');
}

export function downloadPaperMarkdown(
  paperName: string,
  grade: string | undefined,
  groups: ExportGroup[],
) {
  const content = buildPaperMarkdown(paperName, grade, groups);
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-');
  link.href = url;
  link.download = `${paperName || '试卷'}-${stamp}.md`;
  link.click();
  URL.revokeObjectURL(url);
}
