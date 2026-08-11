package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.AiGenerationRecordQuery;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.AiGenerationRecordMapper;
import com.nexora.service.AiGenerationRecordService;
import com.nexora.utils.StringTools;


/**
 * AI生成记录表 业务接口实现
 */
@Service("aiGenerationRecordService")
public class AiGenerationRecordServiceImpl implements AiGenerationRecordService {

	@Resource
	private AiGenerationRecordMapper<AiGenerationRecord, AiGenerationRecordQuery> aiGenerationRecordMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<AiGenerationRecord> findListByParam(AiGenerationRecordQuery param) {
		return this.aiGenerationRecordMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AiGenerationRecordQuery param) {
		return this.aiGenerationRecordMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<AiGenerationRecord> findListByPage(AiGenerationRecordQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<AiGenerationRecord> list = this.findListByParam(param);
		PaginationResultVO<AiGenerationRecord> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(AiGenerationRecord bean) {
		return this.aiGenerationRecordMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<AiGenerationRecord> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.aiGenerationRecordMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<AiGenerationRecord> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.aiGenerationRecordMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(AiGenerationRecord bean, AiGenerationRecordQuery param) {
		StringTools.checkParam(param);
		return this.aiGenerationRecordMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AiGenerationRecordQuery param) {
		StringTools.checkParam(param);
		return this.aiGenerationRecordMapper.deleteByParam(param);
	}

	/**
	 * 根据RecordId获取对象
	 */
	@Override
	public AiGenerationRecord getAiGenerationRecordByRecordId(String recordId) {
		return this.aiGenerationRecordMapper.selectByRecordId(recordId);
	}

	/**
	 * 根据RecordId修改
	 */
	@Override
	public Integer updateAiGenerationRecordByRecordId(AiGenerationRecord bean, String recordId) {
		return this.aiGenerationRecordMapper.updateByRecordId(bean, recordId);
	}

	/**
	 * 根据RecordId删除
	 */
	@Override
	public Integer deleteAiGenerationRecordByRecordId(String recordId) {
		return this.aiGenerationRecordMapper.deleteByRecordId(recordId);
	}
}