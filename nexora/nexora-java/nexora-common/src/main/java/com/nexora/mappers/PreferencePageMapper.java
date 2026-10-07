package com.nexora.mappers;

import com.nexora.entity.vo.PreferencePageVO;
import org.apache.ibatis.annotations.Param;

/**
 * 《我的学习偏好》系统页读写（计划 C3，手写精简 mapper：只碰 system_type='PREFERENCE' 的行）。
 *
 * 之所以单独写 mapper：偏好页需要 system_type 这个新字段，而知识页主体的 PO/XML 是生成式的，
 * 单独一套语句可以避免动生成文件，也让保护逻辑（判定某 docId 是否为偏好页）集中在一处。
 */
public interface PreferencePageMapper {

    /** 取某个学生的偏好页（不存在返回 null） */
    PreferencePageVO selectByOwner(@Param("ownerId") String ownerId);

    /** 新建偏好页（system_type=PREFERENCE，恒不参与向量化） */
    Integer insertPage(@Param("docId") String docId,
                       @Param("ownerId") String ownerId,
                       @Param("stage") String stage,
                       @Param("title") String title,
                       @Param("content") String content);

    /** 覆盖标题与正文（不改变归属与状态） */
    Integer updateContent(@Param("docId") String docId,
                          @Param("title") String title,
                          @Param("content") String content);

    /** 该 docId 是否是"这个学生的偏好页"（删除/移动/入库/编辑前的保护判定） */
    Integer countPreferencePage(@Param("docId") String docId, @Param("ownerId") String ownerId);
}
