package com.easymall.mappers;

import com.easymall.entity.po.SysCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作接口
 */
public interface SysCategoryMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据CategoryId更新
     */
    Integer updateByCategoryId(@Param("bean") T t, @Param("categoryId") String categoryId);


    /**
     * 根据CategoryId删除
     */
    Integer deleteByCategoryId(@Param("categoryId") String categoryId);


    /**
     * 根据CategoryId获取对象
     */
    T selectByCategoryId(@Param("categoryId") String categoryId);

    Integer selectMaxSort(@Param("pCategoryId") String pCategoryId);

    void updateSortBatch(@Param("categoryList") List<SysCategory> categoryList);
}
