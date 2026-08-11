package com.smart.campus.service;

import com.smart.campus.entity.po.SystemMenu;
import com.smart.campus.entity.query.SystemMenuQuery;
import com.smart.campus.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 系统菜单权限表 业务接口
 */
public interface SystemMenuService {

    /**
     * 根据条件查询列表
     */
    List<SystemMenu> findListByParam(SystemMenuQuery param);

    /**
     * 根据条件查询数量
     */
    Integer findCountByParam(SystemMenuQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<SystemMenu> findListByPage(SystemMenuQuery param);

    /**
     * 新增
     */
    Integer add(SystemMenu bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<SystemMenu> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<SystemMenu> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(SystemMenu bean, SystemMenuQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(SystemMenuQuery param);

    /**
     * 根据MenuId查询对象
     */
    SystemMenu getSystemMenuByMenuId(Integer menuId);

    /**
     * 根据MenuId修改
     */
    Integer updateSystemMenuByMenuId(SystemMenu bean, Integer menuId);

    /**
     * 根据MenuId删除
     */
    Integer deleteSystemMenuByMenuId(Integer menuId);

    /**
     * 根据MenuCode查询对象
     */
    SystemMenu getSystemMenuByMenuCode(String menuCode);
}
