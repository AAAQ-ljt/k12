package com.nexora.mappers;

import com.nexora.entity.po.ResourceDirectory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源目录表 数据库操作接口
 */
public interface ResourceDirectoryMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据目录ID查询
     */
    T selectByDirId(@Param("dirId") String dirId);

    /**
     * 根据目录ID更新
     */
    Integer updateByDirId(@Param("bean") T t, @Param("dirId") String dirId);

    /**
     * 根据目录ID删除
     */
    Integer deleteByDirId(@Param("dirId") String dirId);

    /**
     * 批量更新同级排序
     */
    Integer updateSortBatch(@Param("list") List<ResourceDirectory> list);
}
