-- =====================================================================
-- 编程题输出契约回填 · 第三批：高中 1/2（cp-sr-01 ~ cp-sr-12）
--
-- 方法同前：按参考答案真实输出写 output_spec，换一组数据写 output_example。
-- 幂等：按 problem_id 更新。执行：mysql -uroot -p nexora < 20261013_coding_output_backfill_senior_a.sql
-- =====================================================================

SET NAMES utf8mb4;

-- cp-sr-01 冒泡排序过程打印
UPDATE coding_problem SET
  output_spec = '先按轮次输出，每轮一行「第 n 轮后： 」+ 当前列表；最后一行为「排序完成： 」+ 最终列表（方括号、元素间「, 」分隔）。',
  output_example = '格式示例（换成 3 个元素的数组）：\n第 1 轮后： [2, 1, 3]\n第 2 轮后： [1, 2, 3]\n排序完成： [1, 2, 3]'
WHERE problem_id = 'cp-sr-01';

-- cp-sr-02 选择排序与交换次数
UPDATE coding_problem SET
  output_spec = '共 2 行：第 1 行「排序结果： 」+ 升序列表；第 2 行「交换次数： N」。',
  output_example = '示例（换一组数）：\n排序结果： [1, 4, 9]\n交换次数： 2'
WHERE problem_id = 'cp-sr-02';

-- cp-sr-03 二分查找实现
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「目标 X 下标为 i ，比较 c 次」（逗号前有空格；找不到时输出「目标 X 不存在」）。',
  output_example = '示例（另一组数据）：\n目标 7 下标为 2 ，比较 2 次'
WHERE problem_id = 'cp-sr-03';

-- cp-sr-04 词频 Top5
UPDATE coding_problem SET
  output_spec = '按词频从高到低输出（最多 5 行），每行「词：N 次，占比 P%」，占比保留 1 位小数。',
  output_example = '示例（换一段文本）：\n苹果：2 次，占比 50.0%\n香蕉：1 次，占比 25.0%\n西瓜：1 次，占比 25.0%',
  numeric_tolerant = 1
WHERE problem_id = 'cp-sr-04';

-- cp-sr-05 括号匹配
UPDATE coding_problem SET
  output_spec = '每个括号序列占一行，格式「序列 → 合法」或「序列 → 不合法」（箭头两侧各一个空格）。',
  output_example = '示例（另外几个序列）：\n()[]{} → 合法\n([{}]) → 合法\n((() → 不合法'
WHERE problem_id = 'cp-sr-05';

-- cp-sr-06 最大子段和
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「最大子段和： N」。',
  output_example = '示例（换成 [-2, 1, -3, 4] ）：\n最大子段和： 4'
WHERE problem_id = 'cp-sr-06';

-- cp-sr-07 递归与记忆化
UPDATE coding_problem SET
  output_spec = '共 4 行：两两一组，分别输出朴素递归与记忆化版本的「fib(n) = 值」和「耗时： m ms」（耗时随机器不同，允许差异）。',
  output_example = '格式示例（换成 n=20）：\n朴素递归 fib(20) = 6765\n耗时： 8 ms\n记忆化 fib(20) = 6765\n耗时： 0 ms'
WHERE problem_id = 'cp-sr-07';

-- cp-sr-08 快速排序
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「排序结果： 」+ 升序列表。',
  output_example = '示例（换一组数）：\n排序结果： [1, 5, 8]'
WHERE problem_id = 'cp-sr-08';

-- cp-sr-09 归并排序
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「排序结果： 」+ 升序列表。',
  output_example = '示例（换一组数）：\n排序结果： [2, 6, 9]'
WHERE problem_id = 'cp-sr-09';

-- cp-sr-10 埃氏筛素数
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「100 以内素数共 N 个： 」+ 素数列表（升序，方括号、元素间「, 」），个数要与列表长度一致。',
  output_example = '格式示例（改成 30 以内）：\n30 以内素数共 10 个： [2, 3, 5, 7, 11, 13, 17, 19, 23, 29]'
WHERE problem_id = 'cp-sr-10';

-- cp-sr-11 最长无重复子串
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「最长无重复子串： 子串 ，长度 N」（子串与长度前的空格按参考格式保留）。',
  output_example = '示例（换成 "abba"）：\n最长无重复子串： ab ，长度 2'
WHERE problem_id = 'cp-sr-11';

-- cp-sr-12 链表反转
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「反转后： 」+ 反转后的列表（方括号、元素间「, 」分隔）。',
  output_example = '示例（换成 1→2→3）：\n反转后： [3, 2, 1]'
WHERE problem_id = 'cp-sr-12';

SELECT problem_id FROM coding_problem
WHERE problem_id IN ('cp-sr-01','cp-sr-02','cp-sr-03','cp-sr-04','cp-sr-05','cp-sr-06',
                     'cp-sr-07','cp-sr-08','cp-sr-09','cp-sr-10','cp-sr-11','cp-sr-12')
  AND (output_spec IS NULL OR CHAR_LENGTH(output_spec) = 0 OR output_example IS NULL OR CHAR_LENGTH(output_example) = 0);
