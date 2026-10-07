-- =====================================================================
-- 编程题输出契约回填 · 第一批：小学高年级 7 道（cp-ph-05 已在此前补齐）
--
-- 背景：output_spec（输出要求）/ output_example（输出示例）是后来加的字段，
--       存量题没有填，学生做题看不到"要输出成什么样"。本脚本按**参考答案的真实输出**逐题写：
--       · output_spec   = 输出要求（几行、格式、标点、小数位）
--       · output_example = 用**另一组数据**演示格式（绝不使用本题数据，避免泄题）
--
-- 幂等：按 problem_id 更新，可重复执行。
-- 执行方式：mysql -uroot -p nexora < 20261013_coding_output_backfill.sql
-- 【禁止改已存在的表结构】本脚本只更新数据。
-- =====================================================================

SET NAMES utf8mb4;

-- cp-ph-01 星星塔（5 层）
UPDATE coding_problem SET
  output_spec = '按层打印 5 行：第 1 行 1 个 ★、第 2 行 2 个……第 5 行 5 个；每行顶格、末尾不留多余空格；最后单独一行输出「塔搭好啦！一共 5 层」（感叹号用英文半角）。',
  output_example = '以 3 层为例，完整输出是：\n★\n★★\n★★★\n塔搭好啦！一共 3 层'
WHERE problem_id = 'cp-ph-01';

-- cp-ph-02 九九乘法表
UPDATE coding_problem SET
  output_spec = '共 9 行；第 n 行包含 1×n 到 n×n 共 n 个算式，乘号用全角「×」、等号用「=」，算式之间用两个空格分隔，行首不留空格。',
  output_example = '格式示例（只演示前两行，注意算式之间是两个空格）：\n1×1=1  \n1×2=2  2×2=4'
WHERE problem_id = 'cp-ph-02';

-- cp-ph-03 猜数字（二分法）
UPDATE coding_problem SET
  output_spec = '每猜一次输出一行「第 n 次猜： xx」（冒号用全角「：」、冒号后一个空格，n 从 1 开始）；最后一行输出猜中的提示，写明一共猜了几次。',
  output_example = '示例（换一个神秘数字演示格式）：\n第 1 次猜： 25\n第 2 次猜： 13\n猜中啦！一共猜了 2 次'
WHERE problem_id = 'cp-ph-03';

-- cp-ph-04 成绩单统计（平均分保留 1 位小数，纯数值行按数值比较）
UPDATE coding_problem SET
  output_spec = '共 4 行，依次是平均分、最高分、最低分、及格人数；冒号用全角「：」且后跟一个空格；平均分四舍五入保留 1 位小数，人数写法为「N 人」。',
  output_example = '示例（换一组成绩演示格式）：\n平均分： 85.0\n最高分： 98\n最低分： 61\n及格人数： 5 人',
  numeric_tolerant = 1
WHERE problem_id = 'cp-ph-04';

-- cp-ph-06 杨辉三角
UPDATE coding_problem SET
  output_spec = '共 6 行；每行格式为「杨辉三角第 n 行： 」后接 Python 列表（方括号、元素之间用「, 」分隔，不加空格以外的字符）。',
  output_example = '格式示例（只演示前两行）：\n杨辉三角第 1 行： [1]\n杨辉三角第 2 行： [1, 1]'
WHERE problem_id = 'cp-ph-06';

-- cp-ph-07 质数筛选
UPDATE coding_problem SET
  output_spec = '只输出 1 行：「2~50 的质数共 N 个： 」后接 Python 列表（方括号、元素之间用「, 」分隔、升序），个数 N 要与列表长度一致。',
  output_example = '格式示例（换成 2~10 的范围）：\n2~10 的质数共 4 个： [2, 3, 5, 7]'
WHERE problem_id = 'cp-ph-07';

-- cp-ph-08 递归画分形
UPDATE coding_problem SET
  output_spec = '共 9 行：前 4 行为逐行收窄的星形三角（第 1 行 7 个 ★ 顶格，之后每行行首多 1 个空格、星数减 2），中间 4 行与前面上下镜像，最后一行输出「分形绘制完成」。',
  output_example = '示例（换成 3 个 ★ 的规模，说明收窄规律）：\n★★★\n ★\n ★\n★★★\n分形绘制完成'
WHERE problem_id = 'cp-ph-08';

-- 核对：本批 7 道题都应有契约（返回 0 行即为合格）
SELECT problem_id, title
FROM coding_problem
WHERE problem_id IN ('cp-ph-01','cp-ph-02','cp-ph-03','cp-ph-04','cp-ph-06','cp-ph-07','cp-ph-08')
  AND (output_spec IS NULL OR CHAR_LENGTH(output_spec) = 0
       OR output_example IS NULL OR CHAR_LENGTH(output_example) = 0);
