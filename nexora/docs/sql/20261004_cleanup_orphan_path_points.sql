-- 2026-10-04 清理"学习路径自动创建"的孤儿知识点及其掌握度
--
-- 背景：旧版删除学习路径只级联删除 learning_path_item，未清理 resolveOrCreatePoint 自动创建的知识点；
-- 这些孤儿知识点（无任何路线引用）的掌握度会继续计入学生「学习概览-进行中」与掌握度统计
-- （用户反馈实据：liyekai 删除路线后仍显示"进行中 2"，对应"照片滤镜与贴纸""变量与数据类型"两条）。
-- 新代码已让 deletePath 自动清理（cleanupOrphanPoints），本脚本清理存量数据。
--
-- 安全边界：仅删除 description 以"由学习路径自动创建"开头、且无 learning_path_item 引用、
-- 且无 knowledge_doc 引用的知识点；仍被其它路线引用或已挂文档的知识点一律保留。

-- 1) 预览孤儿清单（先看数量与内容）
SELECT p.knowledge_point_id, p.stage, p.name
FROM knowledge_point p
WHERE p.description LIKE '由学习路径自动创建%'
  AND NOT EXISTS (SELECT 1 FROM learning_path_item i WHERE i.knowledge_point_id = p.knowledge_point_id)
  AND NOT EXISTS (SELECT 1 FROM knowledge_doc d WHERE d.knowledge_point_id = p.knowledge_point_id);

-- 2) 删除孤儿知识点的掌握度（点已无归属，连带所有学生对该点的记录）
DELETE m FROM knowledge_mastery m
JOIN knowledge_point p ON p.knowledge_point_id = m.knowledge_point_id
WHERE p.description LIKE '由学习路径自动创建%'
  AND NOT EXISTS (SELECT 1 FROM learning_path_item i WHERE i.knowledge_point_id = p.knowledge_point_id)
  AND NOT EXISTS (SELECT 1 FROM knowledge_doc d WHERE d.knowledge_point_id = p.knowledge_point_id);

-- 3) 删除孤儿知识点本体
DELETE p FROM knowledge_point p
WHERE p.description LIKE '由学习路径自动创建%'
  AND NOT EXISTS (SELECT 1 FROM learning_path_item i WHERE i.knowledge_point_id = p.knowledge_point_id)
  AND NOT EXISTS (SELECT 1 FROM knowledge_doc d WHERE d.knowledge_point_id = p.knowledge_point_id);

-- 4) 校验：残留孤儿应为 0
SELECT COUNT(*) AS orphan_left FROM knowledge_point p
WHERE p.description LIKE '由学习路径自动创建%'
  AND NOT EXISTS (SELECT 1 FROM learning_path_item i WHERE i.knowledge_point_id = p.knowledge_point_id);
