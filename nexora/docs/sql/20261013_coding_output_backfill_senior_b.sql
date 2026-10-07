-- =====================================================================
-- 编程题输出契约回填 · 第三批(B)：高中 cp-sr-13 ~ cp-sr-24（12 道，收尾批次）
--
-- 方法同前：按参考答案真实输出写 output_spec，换一组数据写 output_example。
-- 幂等：按 problem_id 更新。执行：mysql -uroot -p nexora < 20261013_coding_output_backfill_senior_b.sql
-- =====================================================================

SET NAMES utf8mb4;

-- cp-sr-13 循环队列模拟
UPDATE coding_problem SET
  output_spec = '每次入队输出一行「值 入队，队列： 」+ 当前队列（用列表表示，空位写 None）；队列满时输出「队列已满， 值 入队失败」。',
  output_example = '格式示例（容量 2）：\n1 入队，队列： [1, None]\n2 入队，队列： [1, 2]\n队列已满， 3 入队失败'
WHERE problem_id = 'cp-sr-13';

-- cp-sr-14 后缀表达式求值
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「表达式 = 结果」（等号两侧各一个空格；表达式保留原样的空格分隔）。',
  output_example = '示例（换成 2 3 + ）：\n2 3 +  = 5'
WHERE problem_id = 'cp-sr-14';

-- cp-sr-15 二叉树层序遍历
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「层序遍历： 」+ 按层序排列的列表（方括号、元素间「, 」）。',
  output_example = '格式示例（换成 3 个结点）：\n层序遍历： [1, 2, 3]'
WHERE problem_id = 'cp-sr-15';

-- cp-sr-16 爬楼梯（动态规划）
UPDATE coding_problem SET
  output_spec = '每个 n 占一行：「爬到第 n 阶共有 M 种走法」；题目要求的每个 n 都要输出。',
  output_example = '示例（换成第 3 阶与第 4 阶）：\n爬到第 3 阶共有 3 种走法\n爬到第 4 阶共有 5 种走法'
WHERE problem_id = 'cp-sr-16';

-- cp-sr-17 零钱兑换
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「凑出 N 元最少需要 M 枚硬币」。',
  output_example = '示例（目标金额 6）：\n凑出 6 元最少需要 2 枚硬币'
WHERE problem_id = 'cp-sr-17';

-- cp-sr-18 最长公共子序列
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「最长公共子序列长度： N」。',
  output_example = '示例（换一对字符串，答案为 2）：\n最长公共子序列长度： 2'
WHERE problem_id = 'cp-sr-18';

-- cp-sr-19 图的 BFS 最短路
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「起点 到 终点 的最短跳数： N」。',
  output_example = '格式示例（换成 A 到 D）：\nA 到 D 的最短跳数： 2'
WHERE problem_id = 'cp-sr-19';

-- cp-sr-20 图的连通分量
UPDATE coding_problem SET
  output_spec = '第 1 行输出「连通分量个数： N」；随后每个分量占一行「  分量： [结点列表]」（行首两个空格）。',
  output_example = '格式示例（换成两个分量）：\n连通分量个数： 2\n  分量： [0, 1]\n  分量： [2, 3, 4]'
WHERE problem_id = 'cp-sr-20';

-- cp-sr-21 并查集：朋友圈
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「朋友圈数量： N」。',
  output_example = '示例（换一组关系，答案为 2）：\n朋友圈数量： 2'
WHERE problem_id = 'cp-sr-21';

-- cp-sr-22 下一个更大元素
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「下一个更大元素： 」+ 列表（每个位置放右侧第一个更大的数，没有则写 -1）。',
  output_example = '示例（换成 [2, 1, 3]）：\n下一个更大元素： [3, 3, -1]'
WHERE problem_id = 'cp-sr-22';

-- cp-sr-23 学生管理类
UPDATE coding_problem SET
  output_spec = '每个学生占一行：「姓名 平均分 X 等级 Y」（平均分保留 1 位小数；等级按 优秀/良好/及格/待提高 划分）。',
  output_example = '示例（换成另一个学生）：\n小李 平均分 78.5 等级 良好'
WHERE problem_id = 'cp-sr-23';

-- cp-sr-24 简易计算器
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「表达式 = 结果」（等号两侧各一个空格，表达式与题目给定写法一致；结果为整数时不带小数点）。',
  output_example = '示例（换成 2 + 3 * 4 ）：\n2 + 3 * 4 = 14'
WHERE problem_id = 'cp-sr-24';

SELECT COUNT(*) AS 仍缺契约 FROM coding_problem
WHERE output_spec IS NULL OR CHAR_LENGTH(output_spec) = 0
   OR output_example IS NULL OR CHAR_LENGTH(output_example) = 0;
