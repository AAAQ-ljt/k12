package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.SystemConfigQuery;
import com.nexora.entity.po.SystemConfig;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.SystemConfigMapper;
import com.nexora.service.SystemConfigService;
import com.nexora.utils.StringTools;


/**
 * 系统全局配置表 业务接口实现
 */
@Service("systemConfigService")
public class SystemConfigServiceImpl implements SystemConfigService {

	@Resource
	private SystemConfigMapper<SystemConfig, SystemConfigQuery> systemConfigMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SystemConfig> findListByParam(SystemConfigQuery param) {
		return this.systemConfigMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SystemConfigQuery param) {
		return this.systemConfigMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SystemConfig> findListByPage(SystemConfigQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SystemConfig> list = this.findListByParam(param);
		PaginationResultVO<SystemConfig> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SystemConfig bean) {
		return this.systemConfigMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SystemConfig> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.systemConfigMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SystemConfig> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.systemConfigMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SystemConfig bean, SystemConfigQuery param) {
		StringTools.checkParam(param);
		return this.systemConfigMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SystemConfigQuery param) {
		StringTools.checkParam(param);
		return this.systemConfigMapper.deleteByParam(param);
	}

	/**
	 * 根据ConfigId获取对象
	 */
	@Override
	public SystemConfig getSystemConfigByConfigId(Integer configId) {
		return this.systemConfigMapper.selectByConfigId(configId);
	}

	/**
	 * 根据ConfigId修改
	 */
	@Override
	public Integer updateSystemConfigByConfigId(SystemConfig bean, Integer configId) {
		return this.systemConfigMapper.updateByConfigId(bean, configId);
	}

	/**
	 * 根据ConfigId删除
	 */
	@Override
	public Integer deleteSystemConfigByConfigId(Integer configId) {
		return this.systemConfigMapper.deleteByConfigId(configId);
	}

	/**
	 * 根据ConfigGroupAndConfigKey获取对象
	 */
	@Override
	public SystemConfig getSystemConfigByConfigGroupAndConfigKey(String configGroup, String configKey) {
		return this.systemConfigMapper.selectByConfigGroupAndConfigKey(configGroup, configKey);
	}

	/**
	 * 根据ConfigGroupAndConfigKey修改
	 */
	@Override
	public Integer updateSystemConfigByConfigGroupAndConfigKey(SystemConfig bean, String configGroup, String configKey) {
		return this.systemConfigMapper.updateByConfigGroupAndConfigKey(bean, configGroup, configKey);
	}

	/**
	 * 根据ConfigGroupAndConfigKey删除
	 */
	@Override
	public Integer deleteSystemConfigByConfigGroupAndConfigKey(String configGroup, String configKey) {
		return this.systemConfigMapper.deleteByConfigGroupAndConfigKey(configGroup, configKey);
	}
}