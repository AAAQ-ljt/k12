package com.easymall.service;

import com.easymall.entity.po.SysProductProperty;
import com.easymall.entity.query.SysProductPropertyQuery;
import com.easymall.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 业务接口
 */
public interface SysProductPropertyService {

    /**
     * 根据条件查询列表
     */
    List<SysProductProperty> findListByParam(SysProductPropertyQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(SysProductPropertyQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<SysProductProperty> findListByPage(SysProductPropertyQuery param);

    /**
     * 新增
     */
    Integer add(SysProductProperty bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<SysProductProperty> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<SysProductProperty> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(SysProductProperty bean, SysProductPropertyQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(SysProductPropertyQuery param);

    /**
     * 根据PropertyId查询对象
     */
    SysProductProperty getSysProductPropertyByPropertyId(String propertyId);


    /**
     * 根据PropertyId修改
     */
    Integer updateSysProductPropertyByPropertyId(SysProductProperty bean, String propertyId);


    /**
     * 根据PropertyId删除
     */
    Integer deleteSysProductPropertyByPropertyId(String propertyId);

    void saveSysProductPropertyService(SysProductProperty productProperty);

    void deleteSysProductPropertyService(String propertyId);
}