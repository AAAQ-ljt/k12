import { request } from './request';

export interface LearningOverview {
  studentCount: number;
  courseActiveStudents: number;
  courseAvgProgress: number;
  courseTotalDuration: number;
  practiceTotal: number;
  practiceCorrect: number;
  practiceAccuracy: number;
  wikiResourceCount: number;
  wikiActiveUsers: number;
  aiMessageCount: number;
  aiActiveUsers: number;
  aiTotalTokens: number;
  masteryAvgScore: number;
}

export function loadLearningOverview(): Promise<LearningOverview> {
  return request.get('/learningAnalysis/overview');
}
