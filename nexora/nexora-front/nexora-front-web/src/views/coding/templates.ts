/**
 * 编程实验室的任务卡与预置代码框架（按学段分层）。
 *
 * 设计口径（对齐赛事「教育适配性」）：
 * - 小学高年级：图形/游戏向，先看到效果再理解循环与条件；
 * - 初中：列表、函数、字符串处理等 Python 基础；
 * - 高中：算法（排序/递归）与效率意识。
 *
 * 模板一律不依赖 input()（浏览器环境没有终端交互），输入类题目改为「模拟数据」演示。
 */

export interface CodingTask {
  /** 关卡标题 */
  title: string;
  /** 一句话目标（学生要达成什么） */
  goal: string;
  /** 思路提示（可折叠） */
  hint: string;
  /** 通关判定关键词：运行输出包含其中任一项即点亮 ⭐（轻量前端判定，不做严格判分） */
  expectedKeywords?: string[];
  /** 预置代码框架 */
  code: string;
}

export const CODING_TASKS: Record<string, CodingTask[]> = {
  PRIMARY_HIGH: [
    {
      title: '星星塔（循环与图形）',
      goal: '用循环打印一座 5 层星星塔，观察每一层的星星数量如何随行号变化。',
      hint: '第 i 行打印 i 颗星：先想清楚「重复几次」，再想清楚「每次打印什么」。',
      expectedKeywords: ['★', '*'],
      code: `# 星星塔：每层比上一层多一颗星
layers = 5

for level in range(1, layers + 1):
    print("★" * level)

print("塔搭好啦！一共", layers, "层")
`,
    },
    {
      title: '猜数字（条件与循环）',
      goal: '让程序把 1~50 里的神秘数字猜出来，统计用了几次。',
      hint: '用 while 循环不断接近答案；每次比较后把范围缩小一半（二分查找的雏形）。',
      expectedKeywords: ['猜到', '次数'],
      code: `# 猜数字：程序自己猜，看看要几步
secret = 37          # 神秘数字
low, high = 1, 50
tries = 0

while low <= high:
    guess = (low + high) // 2
    tries = tries + 1
    print("第", tries, "次猜：", guess)
    if guess == secret:
        print("猜到啦！答案就是", secret, "，一共用了", tries, "次")
        break
    elif guess < secret:
        low = guess + 1
    else:
        high = guess - 1
`,
    },
    {
      title: '九九乘法表（双重循环）',
      goal: '打印 1~9 的乘法表，体会「循环里再套一个循环」。',
      hint: '外层控制行（第一个乘数），内层控制列（第二个乘数）。',
      expectedKeywords: ['×', '=', '*'],
      code: `# 九九乘法表
for a in range(1, 10):
    line = ""
    for b in range(1, a + 1):
        line = line + str(b) + "×" + str(a) + "=" + str(a * b) + "  "
    print(line)
`,
    },
  ],
  JUNIOR: [
    {
      title: '成绩单统计（列表与函数）',
      goal: '算出一组成绩的平均分、最高分与及格人数。',
      hint: '用 for 遍历列表；求平均分注意除以长度；及格用 if 判断。',
      expectedKeywords: ['平均', '及格'],
      code: `# 成绩单统计
scores = [88, 92, 57, 73, 100, 64, 81]

total = 0
for score in scores:
    total = total + score

average = total / len(scores)
print("平均分：", round(average, 1))
print("最高分：", max(scores))
print("最低分：", min(scores))

passed = 0
for score in scores:
    if score >= 60:
        passed = passed + 1
print("及格人数：", passed, "人")
`,
    },
    {
      title: '回文判断（字符串）',
      goal: '判断一句话是不是回文（正着读和倒着读一样）。',
      hint: 'Python 里 s[::-1] 可以把字符串倒过来；把标点去掉会更准确。',
      expectedKeywords: ['回文', '不是'],
      code: `# 回文判断
def is_palindrome(text):
    cleaned = ""
    for ch in text:
        if ch.strip() != "":
            cleaned = cleaned + ch
    return cleaned == cleaned[::-1]

for sentence in ["上海自来水来自海上", "人工智能很有趣"]:
    if is_palindrome(sentence):
        print(sentence, "→ 是回文")
    else:
        print(sentence, "→ 不是回文")
`,
    },
    {
      title: '找质数（函数与循环）',
      goal: '找出 2~50 之间的所有质数。',
      hint: '判断 n 是否质数：只要试除到 n 的平方根即可，效率更高。',
      expectedKeywords: ['质数'],
      code: `# 找出 2~50 之间的质数
def is_prime(n):
    if n < 2:
        return False
    i = 2
    while i * i <= n:
        if n % i == 0:
            return False
        i = i + 1
    return True

primes = [n for n in range(2, 51) if is_prime(n)]
print("2~50 的质数共", len(primes), "个：")
print(primes)
`,
    },
  ],
  SENIOR: [
    {
      title: '冒泡排序（算法可视化）',
      goal: '实现冒泡排序，并打印每一轮结束后的数组，看清排序过程。',
      hint: '外层控制轮数，内层两两比较交换；每轮结束最大的元素会「冒」到最后。',
      expectedKeywords: ['第', '轮', '排序'],
      code: `# 冒泡排序：打印每一轮的中间状态
data = [5, 2, 9, 1, 7, 3]

for i in range(len(data) - 1):
    for j in range(len(data) - 1 - i):
        if data[j] > data[j + 1]:
            data[j], data[j + 1] = data[j + 1], data[j]
    print("第", i + 1, "轮后：", data)

print("排序完成：", data)
`,
    },
    {
      title: '斐波那契（递归与记忆化）',
      goal: '对比朴素递归与记忆化求第 35 项斐波那契数的耗时差异。',
      hint: '递归会重复计算；用字典缓存已经算过的结果可以大幅提速。',
      expectedKeywords: ['耗时', 'ms', '秒'],
      code: `# 斐波那契：递归 vs 记忆化
import time

def fib(n):
    if n < 2:
        return n
    return fib(n - 1) + fib(n - 2)

cache = {}
def fib_fast(n):
    if n < 2:
        return n
    if n in cache:
        return cache[n]
    value = fib_fast(n - 1) + fib_fast(n - 2)
    cache[n] = value
    return value

start = time.time()
print("朴素递归 fib(28) =", fib(28))
print("耗时：", round((time.time() - start) * 1000), "ms")

start = time.time()
print("记忆化 fib(28) =", fib_fast(28))
print("耗时：", round((time.time() - start) * 1000), "ms")
`,
    },
    {
      title: '统计词频（字典与排序）',
      goal: '统计一段文字里每个词出现的次数，并按次数从多到少输出前 5 个。',
      hint: '字典累计次数；sorted 配合 key=lambda 排序。',
      expectedKeywords: ['词频', '出现'],
      code: `# 统计词频
text = "人工智能 让 学习 更 有趣 人工智能 让 教学 更 高效 人工智能 助手"

counter = {}
for word in text.split():
    counter[word] = counter.get(word, 0) + 1

print("词频统计结果：")
for word, count in sorted(counter.items(), key=lambda item: -item[1])[:5]:
    print(" ", word, "出现", count, "次")
`,
    },
  ],
};

/** 取某学段的任务列表（未知学段回退初中） */
export function tasksOfStage(stage?: string): CodingTask[] {
  return CODING_TASKS[stage || ''] || CODING_TASKS.JUNIOR;
}
