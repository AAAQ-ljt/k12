package com.nexora.vo;

import java.util.List;

/**
 * 我的页面学习趋势统计（一次聚合 practice_record 得出）
 */
public class LearningTrendVO {

    /** 有练习流水的总学习天数 */
    private int studyDays;

    /** 连续学习天数（含今天） */
    private int streakDays;

    /** 累计练习次数 */
    private int totalPractice;

    /** 近 7 天每日练习次数（按日历天，含今天，倒序） */
    private List<WeekDayVO> weekTrend;

    public int getStudyDays() {
        return studyDays;
    }

    public void setStudyDays(int studyDays) {
        this.studyDays = studyDays;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }

    public int getTotalPractice() {
        return totalPractice;
    }

    public void setTotalPractice(int totalPractice) {
        this.totalPractice = totalPractice;
    }

    public List<WeekDayVO> getWeekTrend() {
        return weekTrend;
    }

    public void setWeekTrend(List<WeekDayVO> weekTrend) {
        this.weekTrend = weekTrend;
    }

    public static class WeekDayVO {

        /** 日期（MM-dd） */
        private String day;

        /** 当日练习次数 */
        private int count;

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}