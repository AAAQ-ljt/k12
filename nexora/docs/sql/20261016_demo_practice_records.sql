-- =====================================================================
-- 计划 C5-4 服务端模拟数据：给"有学习路径的学生"造逐题练习记录
-- （仅用于服务端演示/联调，**本地不需要**；用户 2026-10-07 明确要求）
--
-- 目的：C5-1 之前的历史练习记录没有题面（practice_record.question_text 为空），
--       导致「逐题复盘」无数据可引用。这里为每个有路径节点的学生，在其路径上挑最多 3 个节点，
--       每个节点造 3 条带题面/答案的模拟作答（1 条错 + 2 条对），让 AI 能指名到具体题目。
--
-- 安全边界（务必保持）：
--   · 只插 practice_record，**不写积分、不动掌握度汇总、不动学习路径状态**；
--   · 全部记录 question_id 以 'DEMO-' 开头 → 一键回滚：delete from practice_record where question_id like 'DEMO-%';
--   · 幂等：某学生已有 DEMO 记录时跳过，不重复插。
-- 执行方式：mysql -uroot -p nexora < 20261016_demo_practice_records.sql
-- =====================================================================

SET NAMES utf8mb4;

-- 待造数据的学生 × 节点（每个学生最多 3 个节点，按路径内顺序取）
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_demo_nodes (
  user_id varchar(32) NOT NULL,
  item_id varchar(32) NOT NULL,
  knowledge_point_id varchar(32) NOT NULL,
  node_name varchar(200) NOT NULL,
  stage varchar(20) NOT NULL,
  seq int NOT NULL
);

TRUNCATE TABLE tmp_demo_nodes;

INSERT INTO tmp_demo_nodes (user_id, item_id, knowledge_point_id, node_name, stage, seq)
SELECT user_id, item_id, knowledge_point_id, knowledge_point_name, stage, seq
FROM (
  SELECT i.user_id,
         i.item_id,
         IFNULL(i.knowledge_point_id, '') AS knowledge_point_id,
         IFNULL(i.knowledge_point_name, '未命名节点') AS knowledge_point_name,
         IFNULL(p.stage, 'JUNIOR') AS stage,
         ROW_NUMBER() OVER (PARTITION BY i.user_id ORDER BY i.sort, i.item_id) AS rn
  FROM learning_path_item i
  JOIN learning_path p ON p.path_id = i.path_id
  WHERE IFNULL(i.knowledge_point_name, '') <> ''
    AND NOT EXISTS (
      SELECT 1 FROM practice_record r
      WHERE r.user_id = i.user_id AND r.question_id LIKE 'DEMO-%'
    )
) t
CROSS JOIN (SELECT 1 AS seq UNION ALL SELECT 2 UNION ALL SELECT 3) s
WHERE t.rn <= 3;

-- 三条模拟作答：第 1 条对、第 2 条对、第 3 条错（错题供 AI 复盘引用）
INSERT INTO practice_record
  (user_id, knowledge_point_id, stage, question_id, question_type, user_answer, question_text, correct_answer,
   is_correct, score, duration, source, biz_id, create_time)
SELECT n.user_id,
       n.knowledge_point_id,
       n.stage,
       CONCAT('DEMO-', n.seq),
       1,
       CASE WHEN n.seq = 3 THEN 'B' ELSE 'A' END,
       CASE n.seq
         WHEN 1 THEN CONCAT('关于「', n.node_name, '」，下列说法正确的是？A. 与课标要求一致的核心表述  B. 明显偏离的干扰项  C. 无关描述  D. 与题干矛盾的说法')
         WHEN 2 THEN CONCAT('在实际问题中应用「', n.node_name, '」时，第一步应该做什么？A. 明确已知条件与待求目标  B. 直接套公式  C. 跳过读题  D. 随便试一个数')
         ELSE CONCAT('判断：「', n.node_name, '」只需要记住结论、不需要理解过程。A. 错误  B. 正确')
       END,
       CASE WHEN n.seq = 3 THEN 'A' ELSE 'A' END,
       CASE WHEN n.seq = 3 THEN 0 ELSE 1 END,
       CASE WHEN n.seq = 3 THEN 0 ELSE 20 END,
       35,
       1,
       n.item_id,
       DATE_SUB(NOW(), INTERVAL n.seq DAY)
FROM tmp_demo_nodes n;

SELECT CONCAT('本次生成的 DEMO 记录数：', COUNT(*)) AS result
FROM practice_record WHERE question_id LIKE 'DEMO-%';

SELECT CONCAT('覆盖学生数：', COUNT(DISTINCT user_id), '，覆盖节点数：', COUNT(DISTINCT biz_id)) AS coverage
FROM practice_record WHERE question_id LIKE 'DEMO-%';

SELECT CONCAT('回滚命令：delete from practice_record where question_id like ', CHAR(39), 'DEMO-%', CHAR(39), ';') AS rollback_hint;

DROP TEMPORARY TABLE IF EXISTS tmp_demo_nodes;
