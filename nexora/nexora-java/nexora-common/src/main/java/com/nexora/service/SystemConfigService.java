package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.SystemConfigQuery;
import com.nexora.entity.po.SystemConfig;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 系统全局配置表 业务接口
 */
public interface SystemConfigService {

	/**
	 * 根据条件查询列表
	 */
	List<SystemConfig> findListByParam(SystemConfigQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SystemConfigQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SystemConfig> findListByPage(SystemConfigQuery param);

	/**
	 * 新增
	 */
	Integer add(SystemConfig bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SystemConfig> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SystemConfig> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(SystemConfig bean,SystemConfigQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(SystemConfigQuery param);

	/**
	 * 根据ConfigId查询对象
	 */
	SystemConfig getSystemConfigByConfigId(Integer configId);


	/**
	 * 根据ConfigId修改
	 */
	Integer updateSystemConfigByConfigId(SystemConfig bean,Integer configId);


	/**
	 * 根据ConfigId删除
	 */
	Integer deleteSystemConfigByConfigId(Integer configId);


	/**
	 * 根据ConfigGroupAndConfigKey查询对象
	 */
	SystemConfig getSystemConfigByConfigGroupAndConfigKey(String configGroup,String configKey);


	/**
	 * 根据ConfigGroupAndConfigKey修改
	 */
	Integer updateSystemConfigByConfigGroupAndConfigKey(SystemConfig bean,String configGroup,String configKey);


	/**
	 * 根据ConfigGroupAndConfigKey删除
	 */
	Integer deleteSystemConfigByConfigGroupAndConfigKey(String configGroup,String configKey);

}