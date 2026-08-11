package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.PromptTemplateQuery;
import com.nexora.entity.po.PromptTemplate;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.PromptTemplateMapper;
import com.nexora.service.PromptTemplateService;
import com.nexora.utils.StringTools;


/**
 * 提示词模板表 业务接口实现
 */
@Service("promptTemplateService")
public class PromptTemplateServiceImpl implements PromptTemplateService {

	@Resource
	private PromptTemplateMapper<PromptTemplate, PromptTemplateQuery> promptTemplateMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<PromptTemplate> findListByParam(PromptTemplateQuery param) {
		return this.promptTemplateMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(PromptTemplateQuery param) {
		return this.promptTemplateMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<PromptTemplate> findListByPage(PromptTemplateQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<PromptTemplate> list = this.findListByParam(param);
		PaginationResultVO<PromptTemplate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(PromptTemplate bean) {
		return this.promptTemplateMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<PromptTemplate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.promptTemplateMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<PromptTemplate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.promptTemplateMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(PromptTemplate bean, PromptTemplateQuery param) {
		StringTools.checkParam(param);
		return this.promptTemplateMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(PromptTemplateQuery param) {
		StringTools.checkParam(param);
		return this.promptTemplateMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public PromptTemplate getPromptTemplateById(Integer id) {
		return this.promptTemplateMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updatePromptTemplateById(PromptTemplate bean, Integer id) {
		return this.promptTemplateMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deletePromptTemplateById(Integer id) {
		return this.promptTemplateMapper.deleteById(id);
	}

	/**
	 * 根据StageAndScene获取对象
	 */
	@Override
	public PromptTemplate getPromptTemplateByStageAndScene(String stage, String scene) {
		return this.promptTemplateMapper.selectByStageAndScene(stage, scene);
	}

	/**
	 * 根据StageAndScene修改
	 */
	@Override
	public Integer updatePromptTemplateByStageAndScene(PromptTemplate bean, String stage, String scene) {
		return this.promptTemplateMapper.updateByStageAndScene(bean, stage, scene);
	}

	/**
	 * 根据StageAndScene删除
	 */
	@Override
	public Integer deletePromptTemplateByStageAndScene(String stage, String scene) {
		return this.promptTemplateMapper.deleteByStageAndScene(stage, scene);
	}
}