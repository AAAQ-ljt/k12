package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.SystemMenu;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.query.SystemMenuQuery;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.SystemMenuMapper;
import com.smart.campus.service.SystemMenuService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统菜单权限表 业务接口实现
 */
@Service("systemMenuService")
public class SystemMenuServiceImpl implements SystemMenuService {

    @Resource
    private SystemMenuMapper<SystemMenu, SystemMenuQuery> systemMenuMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<SystemMenu> findListByParam(SystemMenuQuery param) {
        return this.systemMenuMapper.selectList(param);
    }

    /**
     * 根据条件查询数量
     */
    @Override
    public Integer findCountByParam(SystemMenuQuery param) {
        return this.systemMenuMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<SystemMenu> findListByPage(SystemMenuQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<SystemMenu> list = this.findListByParam(param);
        return new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    /**
     * 新增
     */
    @Override
    public Integer add(SystemMenu bean) {
        return this.systemMenuMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<SystemMenu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.systemMenuMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<SystemMenu> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.systemMenuMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(SystemMenu bean, SystemMenuQuery param) {
        StringTools.checkParam(param);
        return this.systemMenuMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(SystemMenuQuery param) {
        StringTools.checkParam(param);
        return this.systemMenuMapper.deleteByParam(param);
    }

    /**
     * 根据MenuId获取对象
     */
    @Override
    public SystemMenu getSystemMenuByMenuId(Integer menuId) {
        return this.systemMenuMapper.selectByMenuId(menuId);
    }

    /**
     * 根据MenuId修改
     */
    @Override
    public Integer updateSystemMenuByMenuId(SystemMenu bean, Integer menuId) {
        return this.systemMenuMapper.updateByMenuId(bean, menuId);
    }

    /**
     * 根据MenuId删除
     */
    @Override
    public Integer deleteSystemMenuByMenuId(Integer menuId) {
        return this.systemMenuMapper.deleteByMenuId(menuId);
    }

    /**
     * 根据MenuCode获取对象
     */
    @Override
    public SystemMenu getSystemMenuByMenuCode(String menuCode) {
        return this.systemMenuMapper.selectByMenuCode(menuCode);
    }
}
