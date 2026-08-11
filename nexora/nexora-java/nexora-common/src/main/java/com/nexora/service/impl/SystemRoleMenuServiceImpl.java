package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.SystemRoleMenuQuery;
import com.nexora.entity.po.SystemRoleMenu;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.SystemRoleMenuMapper;
import com.nexora.service.SystemRoleMenuService;
import com.nexora.utils.StringTools;


/**
 * 角色菜单关联表 业务接口实现
 */
@Service("systemRoleMenuService")
public class SystemRoleMenuServiceImpl implements SystemRoleMenuService {

	@Resource
	private SystemRoleMenuMapper<SystemRoleMenu, SystemRoleMenuQuery> systemRoleMenuMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SystemRoleMenu> findListByParam(SystemRoleMenuQuery param) {
		return this.systemRoleMenuMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SystemRoleMenuQuery param) {
		return this.systemRoleMenuMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SystemRoleMenu> findListByPage(SystemRoleMenuQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SystemRoleMenu> list = this.findListByParam(param);
		PaginationResultVO<SystemRoleMenu> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SystemRoleMenu bean) {
		return this.systemRoleMenuMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SystemRoleMenu> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.systemRoleMenuMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SystemRoleMenu> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.systemRoleMenuMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SystemRoleMenu bean, SystemRoleMenuQuery param) {
		StringTools.checkParam(param);
		return this.systemRoleMenuMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SystemRoleMenuQuery param) {
		StringTools.checkParam(param);
		return this.systemRoleMenuMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public SystemRoleMenu getSystemRoleMenuById(Integer id) {
		return this.systemRoleMenuMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateSystemRoleMenuById(SystemRoleMenu bean, Integer id) {
		return this.systemRoleMenuMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteSystemRoleMenuById(Integer id) {
		return this.systemRoleMenuMapper.deleteById(id);
	}
}