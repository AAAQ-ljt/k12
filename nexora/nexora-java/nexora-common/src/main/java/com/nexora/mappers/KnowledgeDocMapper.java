package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 知识库文档表 数据库操作接口
 */
public interface KnowledgeDocMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据DocId更新
	 */
	 Integer updateByDocId(@Param("bean") T t,@Param("docId") String docId);


	/**
	 * 根据DocId删除
	 */
	 Integer deleteByDocId(@Param("docId") String docId);


	/**
	 * 根据DocId获取对象
	 */
	 T selectByDocId(@Param("docId") String docId);

	/**
	 * 改写所属知识页子文件夹（folderId 允许为 null，=移回知识页根目录）
	 */
	 Integer updateFolderIdByDocId(@Param("folderId") String folderId,
								   @Param("updateTime") Date updateTime,
								   @Param("docId") String docId);

	/**
	 * 批量移回知识页根目录（删除子文件夹时，子树内知识页一并归位）
	 */
	 Integer updateFolderIdToNullBatch(@Param("docIds") List<String> docIds,
									   @Param("updateTime") Date updateTime);


}
