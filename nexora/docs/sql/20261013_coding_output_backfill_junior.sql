-- =====================================================================
-- 编程题输出契约回填 · 第二批：初中 12 道
--
-- 说明：与小高批次同一方法——按参考答案的**真实输出**写 output_spec（输出要求），
--       再换一组数据写 output_example（示例只演示格式，不泄本题答案）。
--       小高批次见 20261013_coding_output_backfill.sql。
--
-- 幂等：按 problem_id 更新，可重复执行。
-- 执行方式：mysql -uroot -p nexora < 20261013_coding_output_backfill_junior.sql
-- 【禁止改已存在的表结构】本脚本只更新数据。
-- =====================================================================

SET NAMES utf8mb4;

-- cp-jr-01 圆的面积（格式化输出）：面积/周长保留 2 位小数，按数值比较
UPDATE coding_problem SET
  output_spec = '共 3 行：第 1 行「半径 x 的圆」；第 2 行「面积：」、第 3 行「周长：」后接数值（冒号用全角，冒号后不留空格；四舍五入保留 2 位小数）。',
  output_example = '示例（换成半径 1.0）：\n半径 1.0 的圆\n面积：3.14\n周长：6.28',
  numeric_tolerant = 1
WHERE problem_id = 'cp-jr-01';

-- cp-jr-02 回文判断：每句一行，箭头后给结论
UPDATE coding_problem SET
  output_spec = '每句占一行，格式为「原句 → 是回文」或「原句 → 不是回文」（箭头两侧各一个空格）；不要输出多余说明行。',
  output_example = '示例（另外两句）：\n星星锁锁星星 → 是回文\n今天天气真好 → 不是回文'
WHERE problem_id = 'cp-jr-02';

-- cp-jr-03 列表去重并排序
UPDATE coding_problem SET
  output_spec = '共 2 行：第 1 行「去重后： 」后接 Python 列表（升序、方括号、元素之间用「, 」分隔）；第 2 行「去掉了 N 个重复值」。',
  output_example = '示例（换一组成绩）：\n去重后： [3, 5, 9]\n去掉了 2 个重复值'
WHERE problem_id = 'cp-jr-03';

-- cp-jr-04 斐波那契前 N 项
UPDATE coding_problem SET
  output_spec = '共 2 行：第 1 行「斐波那契数列前 10 项： 」后接列表（从 1、1 开始，方括号、元素之间用「, 」）；第 2 行「总和： N」。',
  output_example = '格式示例（换成前 6 项）：\n斐波那契数列前 6 项： [1, 1, 2, 3, 5, 8]\n总和： 20'
WHERE problem_id = 'cp-jr-04';

-- cp-jr-05 单词计数
UPDATE coding_problem SET
  output_spec = '每个不同的词占一行，格式为「  词 出现 N 次」（行首保留两个空格）；按出现次数从多到少排列。',
  output_example = '示例（换一句话）：\n  西瓜 出现 2 次\n  好吃 出现 1 次'
WHERE problem_id = 'cp-jr-05';

-- cp-jr-06 合并两个有序数组
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「合并结果： 」后接合并后的升序列表（方括号、元素之间用「, 」）。',
  output_example = '示例（换两个小数组）：\n合并结果： [2, 3, 5, 6]'
WHERE problem_id = 'cp-jr-06';

-- cp-jr-07 二分查找
UPDATE coding_problem SET
  output_spec = '共 2 行：第 1 行「找到 X ，下标是 i ，比较了 c 次」（三个逗号前都有空格，按参考格式写）；第 2 行「查找结束」。',
  output_example = '示例（换一组数据）：\n找到 12 ，下标是 1 ，比较了 3 次\n查找结束'
WHERE problem_id = 'cp-jr-07';

-- cp-jr-08 矩阵转置
UPDATE coding_problem SET
  output_spec = '共 4 行，每行格式「转置后： 」后接转置矩阵的一行（Python 列表格式，元素之间用「, 」分隔）。',
  output_example = '示例（换成 2×3 的矩阵）：\n转置后： [1, 4]\n转置后： [2, 5]\n转置后： [3, 6]'
WHERE problem_id = 'cp-jr-08';

-- cp-jr-09 字符串压缩
UPDATE coding_problem SET
  output_spec = '共 2 行：第 1 行「压缩结果： 」后接压缩串（连续字符写成「字符+次数」）；第 2 行「压缩前长度： N ，压缩后长度： M」。',
  output_example = '示例（换成 xx yyy）：\n压缩结果： x2y3\n压缩前长度： 5 ，压缩后长度： 2'
WHERE problem_id = 'cp-jr-09';

-- cp-jr-10 成绩等级统计：百分比保留 1 位小数，按数值比较
UPDATE coding_problem SET
  output_spec = '共 4 行，优秀、良好、及格、待提高四个等级各一行（都要出现，不能少写某一个）；每行格式「等级：N 人，占 P%」，百分比四舍五入保留 1 位小数。',
  output_example = '示例（换一组成绩）：\n优秀：1 人，占 25.0%\n良好：2 人，占 50.0%\n及格：1 人，占 25.0%\n待提高：0 人，占 0.0%',
  numeric_tolerant = 1
WHERE problem_id = 'cp-jr-10';

-- cp-jr-11 约瑟夫环
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「出列顺序： 」后接列表（按出列先后排列，方括号、元素之间用「, 」分隔）。',
  output_example = '示例（换成 5 个人）：\n出列顺序： [2, 4, 1, 5, 3]'
WHERE problem_id = 'cp-jr-11';

-- cp-jr-12 全排列
UPDATE coding_problem SET
  output_spec = '共 2 行：第 1 行「前 6 种排列： 」后接嵌套列表（按题目要求的顺序取前 6 种，内层列表元素用「, 」分隔）；第 2 行「排列总数： N」。',
  output_example = '格式示例（换成 1~3 的全排列）：\n前 6 种排列： [[1, 2, 3], [1, 3, 2], [2, 1, 3], [2, 3, 1], [3, 1, 2], [3, 2, 1]]\n排列总数： 6'
WHERE problem_id = 'cp-jr-12';

-- 核对：本批 12 道都应已补齐（返回 0 行即合格）
SELECT problem_id, title
FROM coding_problem
WHERE problem_id LIKE 'cp-jr-%'
  AND (output_spec IS NULL OR CHAR_LENGTH(output_spec) = 0
       OR output_example IS NULL OR CHAR_LENGTH(output_example) = 0);
