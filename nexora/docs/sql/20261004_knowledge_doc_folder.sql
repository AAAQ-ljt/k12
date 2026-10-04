-- 2026-10-04 知识页子文件夹：knowledge_doc 增加归属目录
--
-- 背景：知识页（学生个人）此前平铺无目录；本次支持在「知识页」系统目录下自建子文件夹并移动知识页。
-- 子文件夹复用 resource_directory（挂在 dirType='wiki' 的系统目录下、dirType 留空），
-- knowledge_doc 仅新增归属列；NULL = 知识页根目录。官方知识库（owner_id IS NULL）不使用该列。
-- 存量数据无需迁移（全部视为根目录）。

ALTER TABLE knowledge_doc
  ADD COLUMN folder_id varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属知识页子文件夹（resource_directory.dir_id）；NULL=知识页根目录（仅学生个人知识页使用）';
