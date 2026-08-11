package com.easymall.service;

import com.easymall.entity.po.SysCategory;
import com.easymall.entity.query.SysCategoryQuery;
import com.easymall.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 业务接口
 */
public interface SysCategoryService {

    /**
     * 根据条件查询列表
     */
    List<SysCategory> findListByParam(SysCategoryQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(SysCategoryQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<SysCategory> findListByPage(SysCategoryQuery param);

    /**
     * 新增
     */
    Integer add(SysCategory bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<SysCategory> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<SysCategory> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(SysCategory bean, SysCategoryQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(SysCategoryQuery param);

    /**
     * 根据CategoryId查询对象
     */
    SysCategory getSysCategoryByCategoryId(String categoryId);


    /**
     * 根据CategoryId修改
     */
    Integer updateSysCategoryByCategoryId(SysCategory bean, String categoryId);


    /**
     * 根据CategoryId删除
     */
    Integer deleteSysCategoryByCategoryId(String categoryId);

    void saveCategoryInfo(SysCategory bean);

    void delCategory(String categoryId);

    void changeSort(String categoryIds);

    List<SysCategory> getAllCategoryList();
}