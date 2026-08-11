package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.PracticeRecordQuery;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.PracticeRecordMapper;
import com.nexora.service.PracticeRecordService;
import com.nexora.utils.StringTools;


/**
 * 游戏化练习记录表 业务接口实现
 */
@Service("practiceRecordService")
public class PracticeRecordServiceImpl implements PracticeRecordService {

	@Resource
	private PracticeRecordMapper<PracticeRecord, PracticeRecordQuery> practiceRecordMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<PracticeRecord> findListByParam(PracticeRecordQuery param) {
		return this.practiceRecordMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(PracticeRecordQuery param) {
		return this.practiceRecordMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<PracticeRecord> findListByPage(PracticeRecordQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<PracticeRecord> list = this.findListByParam(param);
		PaginationResultVO<PracticeRecord> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(PracticeRecord bean) {
		return this.practiceRecordMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<PracticeRecord> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.practiceRecordMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<PracticeRecord> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.practiceRecordMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(PracticeRecord bean, PracticeRecordQuery param) {
		StringTools.checkParam(param);
		return this.practiceRecordMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(PracticeRecordQuery param) {
		StringTools.checkParam(param);
		return this.practiceRecordMapper.deleteByParam(param);
	}

	/**
	 * 根据RecordId获取对象
	 */
	@Override
	public PracticeRecord getPracticeRecordByRecordId(Long recordId) {
		return this.practiceRecordMapper.selectByRecordId(recordId);
	}

	/**
	 * 根据RecordId修改
	 */
	@Override
	public Integer updatePracticeRecordByRecordId(PracticeRecord bean, Long recordId) {
		return this.practiceRecordMapper.updateByRecordId(bean, recordId);
	}

	/**
	 * 根据RecordId删除
	 */
	@Override
	public Integer deletePracticeRecordByRecordId(Long recordId) {
		return this.practiceRecordMapper.deleteByRecordId(recordId);
	}
}