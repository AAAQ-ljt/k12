-- 编程题关键词判定重做（由 _fix_coding_keywords.py 生成，2026-10-08）
-- 新关键词 = 参考代码输出的每一行；桩代码输出已被自校验拦下
UPDATE coding_problem SET expected_keywords='半径 3.5 的圆,面积：38.48,周长：21.99' WHERE problem_id='cp-jr-01';  -- 圆的面积（格式化输出）
UPDATE coding_problem SET expected_keywords='上海自来水来自海上 → 是回文,人工智能很有趣 → 不是回文' WHERE problem_id='cp-jr-02';  -- 回文判断
UPDATE coding_problem SET expected_keywords='去重后： [57, 73, 81, 88, 92, 100],去掉了 3 个重复值' WHERE problem_id='cp-jr-03';  -- 列表去重并排序
UPDATE coding_problem SET expected_keywords='斐波那契数列前 10 项： [1, 1, 2, 3, 5, 8, 13, 21, 34, 55],总和： 143' WHERE problem_id='cp-jr-04';  -- 斐波那契前 N 项
UPDATE coding_problem SET expected_keywords='  人工智能 出现 2 次,  让 出现 2 次,  更 出现 2 次,  学习 出现 1 次,  有趣 出现 1 次,  教学 出现 1 次,  高效 出现 1 次' WHERE problem_id='cp-jr-05';  -- 单词计数
UPDATE coding_problem SET expected_keywords='合并结果： [1, 2, 3, 4, 7, 8, 9, 10]' WHERE problem_id='cp-jr-06';  -- 合并两个有序数组
UPDATE coding_problem SET expected_keywords='找到 57 ，下标是 5 ，比较了 2 次,查找结束' WHERE problem_id='cp-jr-07';  -- 二分查找
UPDATE coding_problem SET expected_keywords='转置后： [1, 5, 9],转置后： [2, 6, 10],转置后： [3, 7, 11],转置后： [4, 8, 12]' WHERE problem_id='cp-jr-08';  -- 矩阵转置
UPDATE coding_problem SET expected_keywords='压缩结果： a3b2c4d2e1,压缩前长度： 12 ，压缩后长度： 10' WHERE problem_id='cp-jr-09';  -- 字符串压缩
UPDATE coding_problem SET expected_keywords='良好：2 人，占 22.2%,优秀：3 人，占 33.3%,待提高：2 人，占 22.2%,及格：2 人，占 22.2%' WHERE problem_id='cp-jr-10';  -- 成绩等级统计
UPDATE coding_problem SET expected_keywords='出列顺序： [3, 6, 9, 2, 7, 1, 8, 5, 10, 4]' WHERE problem_id='cp-jr-11';  -- 约瑟夫环
UPDATE coding_problem SET expected_keywords='前 6 种排列： [[1, 2, 3, 4], [1, 2, 4, 3], [1, 3, 2, 4], [1, 3, 4, 2], [1, 4, 2, 3], [1, 4, 3, 2]],排列总数： 24' WHERE problem_id='cp-jr-12';  -- 全排列
UPDATE coding_problem SET expected_keywords='★,★★,★★★,★★★★,★★★★★,塔搭好啦！一共 5 层' WHERE problem_id='cp-ph-01';  -- 星星塔
UPDATE coding_problem SET expected_keywords='1×1=1,1×2=2  2×2=4,1×3=3  2×3=6  3×3=9,1×4=4  2×4=8  3×4=12  4×4=16,1×5=5  2×5=10  3×5=15  4×5=20  5×5=25,1×6=6  2×6=12  3×6=18  4×6=24  5×6=30  6×6=36,1×7=7  2×7=14  3×7=21  4×7=28  5×7=35  6×7=42  7×7=49,1×8=8  2×8=16  3×8=24  4×8=32  5×8=40  6×8=48  7×8=56  8×8=64,1×9=9  2×9=18  3×9=27  4×9=36  5×9=45  6×9=54  7×9=63  8×9=72  9×9=81' WHERE problem_id='cp-ph-02';  -- 九九乘法表
UPDATE coding_problem SET expected_keywords='第 1 次猜： 25,第 2 次猜： 38,第 3 次猜： 31,第 4 次猜： 34,第 5 次猜： 36,第 6 次猜： 37,猜到啦！答案就是 37 ，一共用了 6 次' WHERE problem_id='cp-ph-03';  -- 猜数字（二分法）
UPDATE coding_problem SET expected_keywords='平均分： 79.3,最高分： 100,最低分： 57,及格人数： 6 人' WHERE problem_id='cp-ph-04';  -- 成绩单统计
UPDATE coding_problem SET expected_keywords='杨辉三角第 1 行： [1],杨辉三角第 2 行： [1, 1],杨辉三角第 3 行： [1, 2, 1],杨辉三角第 4 行： [1, 3, 3, 1],杨辉三角第 5 行： [1, 4, 6, 4, 1],杨辉三角第 6 行： [1, 5, 10, 10, 5, 1]' WHERE problem_id='cp-ph-06';  -- 杨辉三角
UPDATE coding_problem SET expected_keywords='2~50 的质数共 15 个： [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47]' WHERE problem_id='cp-ph-07';  -- 质数筛选
UPDATE coding_problem SET expected_keywords='★★★★★★★, ★★★★★,  ★★★,   ★,   ★,  ★★★, ★★★★★,★★★★★★★,分形绘制完成' WHERE problem_id='cp-ph-08';  -- 递归画分形
UPDATE coding_problem SET expected_keywords='第 1 轮后： [2, 5, 1, 7, 3, 9],第 2 轮后： [2, 1, 5, 3, 7, 9],第 3 轮后： [1, 2, 3, 5, 7, 9],第 4 轮后： [1, 2, 3, 5, 7, 9],第 5 轮后： [1, 2, 3, 5, 7, 9],排序完成： [1, 2, 3, 5, 7, 9]' WHERE problem_id='cp-sr-01';  -- 冒泡排序过程打印
UPDATE coding_problem SET expected_keywords='排序结果： [11, 12, 22, 25, 64, 90],交换次数： 3' WHERE problem_id='cp-sr-02';  -- 选择排序与交换次数
UPDATE coding_problem SET expected_keywords='目标 23 下标为 3 ，比较 1 次' WHERE problem_id='cp-sr-03';  -- 二分查找实现
UPDATE coding_problem SET expected_keywords='人工智能：3 次，占比 20.0%,更：3 次，占比 20.0%,让：2 次，占比 13.3%,学习：1 次，占比 6.7%,有趣：1 次，占比 6.7%' WHERE problem_id='cp-sr-04';  -- 词频 Top5
UPDATE coding_problem SET expected_keywords='({[]}) → 合法,([)] → 不合法,((( → 不合法' WHERE problem_id='cp-sr-05';  -- 括号匹配
UPDATE coding_problem SET expected_keywords='最大子段和： 6' WHERE problem_id='cp-sr-06';  -- 最大子段和
UPDATE coding_problem SET expected_keywords='朴素递归 fib(28) = 317811,记忆化 fib(28) = 317811,耗时：' WHERE problem_id='cp-sr-07';  -- 递归与记忆化（去掉毫秒数：每次运行都不同，只要求打印结果与「耗时：」标签）
UPDATE coding_problem SET expected_keywords='排序结果： [3, 10, 18, 29, 33, 55, 71]' WHERE problem_id='cp-sr-08';  -- 快速排序
UPDATE coding_problem SET expected_keywords='排序结果： [3, 9, 10, 27, 38, 43, 82]' WHERE problem_id='cp-sr-09';  -- 归并排序
UPDATE coding_problem SET expected_keywords='100 以内素数共 25 个： [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97]' WHERE problem_id='cp-sr-10';  -- 埃氏筛素数
UPDATE coding_problem SET expected_keywords='最长无重复子串： abc ，长度 3' WHERE problem_id='cp-sr-11';  -- 最长无重复子串
UPDATE coding_problem SET expected_keywords='反转后： [5, 4, 3, 2, 1]' WHERE problem_id='cp-sr-12';  -- 链表反转
UPDATE coding_problem SET expected_keywords='1 入队，队列： [1, None, None],2 入队，队列： [1, 2, None],3 入队，队列： [1, 2, 3],队列已满， 4 入队失败,1 出队，队列： [None, 2, 3],5 入队，队列： [5, 2, 3],2 出队，队列： [5, None, 3],3 出队，队列： [5, None, None],5 出队，队列： [None, None, None]' WHERE problem_id='cp-sr-13';  -- 循环队列模拟
UPDATE coding_problem SET expected_keywords='3 4 + 5 *  = 35' WHERE problem_id='cp-sr-14';  -- 后缀表达式求值
UPDATE coding_problem SET expected_keywords='层序遍历： [1, 2, 3, 4, 5, 6, 7]' WHERE problem_id='cp-sr-15';  -- 二叉树层序遍历
UPDATE coding_problem SET expected_keywords='爬到第 5 阶共有 8 种走法,爬到第 10 阶共有 89 种走法' WHERE problem_id='cp-sr-16';  -- 爬楼梯（动态规划）
UPDATE coding_problem SET expected_keywords='凑出 11 元最少需要 3 枚硬币' WHERE problem_id='cp-sr-17';  -- 零钱兑换
UPDATE coding_problem SET expected_keywords='最长公共子序列长度： 3' WHERE problem_id='cp-sr-18';  -- 最长公共子序列
UPDATE coding_problem SET expected_keywords='A 到 G 的最短跳数： 3' WHERE problem_id='cp-sr-19';  -- 图的 BFS 最短路
UPDATE coding_problem SET expected_keywords='连通分量个数： 3,  分量： [0, 1, 2],  分量： [3, 4],  分量： [5, 6]' WHERE problem_id='cp-sr-20';  -- 图的连通分量
UPDATE coding_problem SET expected_keywords='朋友圈数量： 3' WHERE problem_id='cp-sr-21';  -- 并查集：朋友圈
UPDATE coding_problem SET expected_keywords='下一个更大元素： [4, 2, 4, -1, -1]' WHERE problem_id='cp-sr-22';  -- 下一个更大元素
UPDATE coding_problem SET expected_keywords='小红 平均分 94.7 等级 优秀,小明 平均分 86.3 等级 良好,小刚 平均分 65.7 等级 及格' WHERE problem_id='cp-sr-23';  -- 学生管理类
UPDATE coding_problem SET expected_keywords='3 + 4 * (2 - 1) = 7' WHERE problem_id='cp-sr-24';  -- 简易计算器
