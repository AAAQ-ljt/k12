package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.ResourceInfoMapper;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;


/**
 * 资源信息表 业务接口实现
 */
@Service("resourceInfoService")
public class ResourceInfoServiceImpl implements ResourceInfoService {

	@Resource
	private ResourceInfoMapper<ResourceInfo, ResourceInfoQuery> resourceInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ResourceInfo> findListByParam(ResourceInfoQuery param) {
		return this.resourceInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ResourceInfoQuery param) {
		return this.resourceInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ResourceInfo> findListByPage(ResourceInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ResourceInfo> list = this.findListByParam(param);
		PaginationResultVO<ResourceInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ResourceInfo bean) {
		return this.resourceInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ResourceInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.resourceInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ResourceInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.resourceInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ResourceInfo bean, ResourceInfoQuery param) {
		StringTools.checkParam(param);
		return this.resourceInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ResourceInfoQuery param) {
		StringTools.checkParam(param);
		return this.resourceInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据ResourceId获取对象
	 */
	@Override
	public ResourceInfo getResourceInfoByResourceId(String resourceId) {
		return this.resourceInfoMapper.selectByResourceId(resourceId);
	}

	/**
	 * 根据ResourceId修改
	 */
	@Override
	public Integer updateResourceInfoByResourceId(ResourceInfo bean, String resourceId) {
		return this.resourceInfoMapper.updateByResourceId(bean, resourceId);
	}

	/**
	 * 根据ResourceId删除
	 */
	@Override
	public Integer deleteResourceInfoByResourceId(String resourceId) {
		return this.resourceInfoMapper.deleteByResourceId(resourceId);
	}

	/**
	 * 批量更新所属目录
	 */
	@Override
	public Integer updateDirectoryBatch(List<ResourceInfo> list) {
		if (list == null || list.isEmpty()) {
			return 0;
		}
		return this.resourceInfoMapper.updateDirectoryBatch(list);
	}
}
