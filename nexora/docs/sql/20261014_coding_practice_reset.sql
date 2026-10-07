-- =====================================================================
-- 编程练习清档：清除所有学生的编程答题痕迹与对应积分（让学生重新答题）
--
-- 背景与口径：
--   · 练习模式"答过"的痕迹 = ① student_point_record 里 biz_type='CODING_PROBLEM' 的流水
--     （幂等键 user_id+CODING_PROBLEM+题目ID，正因为有它才不能重复得分）
--     ② coding_problem_record（每题练习记录表）
--     ③ Redis 的「看过答案」标记 point:coding:answerUsed:*（不清的话 6 小时内重答只发 30%）
--   · 积分只在**判分通过**时发放（judge 里 result[0] 为真才发），单纯运行不通过不发分；
--     因此删除这些流水 = 允许重答并重新得分，同时把账户分数扣回去、等级按阶梯重算。
--   · **比赛提交（coding_contest_record）不动**：比赛成绩是独立体系、不发全局积分，
--     是否清理由业务另行决定（本脚本不触碰）。
--
-- 幂等：可重复执行（先无流水则不动账户）。
-- 执行方式：mysql -uroot -p nexora < 20261014_coding_practice_reset.sql
-- 【禁止改已存在的表结构】本脚本只清理数据。
-- =====================================================================

SET NAMES utf8mb4;

-- ① 先看要清多少（执行前留痕）
SELECT '待清除的编程积分流水' AS item, COUNT(*) AS rows_count, IFNULL(SUM(points), 0) AS points_sum
FROM student_point_record WHERE biz_type = 'CODING_PROBLEM'
UNION ALL
SELECT '待清除的每题练习记录', COUNT(*), 0 FROM coding_problem_record;

-- ② 账户扣回编程积分（按学生汇总后扣减，最低扣到 0）
UPDATE student_point_account a
JOIN (
    SELECT user_id, SUM(points) AS pts
    FROM student_point_record
    WHERE biz_type = 'CODING_PROBLEM'
    GROUP BY user_id
) t ON t.user_id = a.user_id
SET a.total_points = GREATEST(a.total_points - t.pts, 0),
    a.available_points = GREATEST(a.available_points - t.pts, 0),
    a.update_time = now();

-- ③ 按 GAME.LEVEL_STEP 默认阶梯重算等级（口径与 PointLevelComponent 默认值一致）
UPDATE student_point_account
SET level = CASE
        WHEN total_points >= 4500 THEN 10
        WHEN total_points >= 3600 THEN 9
        WHEN total_points >= 2800 THEN 8
        WHEN total_points >= 2100 THEN 7
        WHEN total_points >= 1500 THEN 6
        WHEN total_points >= 1000 THEN 5
        WHEN total_points >= 600 THEN 4
        WHEN total_points >= 300 THEN 3
        WHEN total_points >= 100 THEN 2
        ELSE 1
    END,
    update_time = now();

-- ④ 删除编程积分流水与每题练习记录（学生可重新答题、重新得分）
DELETE FROM student_point_record WHERE biz_type = 'CODING_PROBLEM';
DELETE FROM coding_problem_record;

-- ⑤ 核对：都应为 0
SELECT '清理后剩余编程流水' AS item, COUNT(*) AS rows_count FROM student_point_record WHERE biz_type = 'CODING_PROBLEM'
UNION ALL
SELECT '清理后剩余练习记录', COUNT(*) FROM coding_problem_record;
