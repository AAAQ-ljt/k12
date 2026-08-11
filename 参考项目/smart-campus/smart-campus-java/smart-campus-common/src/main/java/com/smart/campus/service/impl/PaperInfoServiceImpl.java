package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.PaperInfoQuery;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.PaperInfoMapper;
import com.smart.campus.service.PaperInfoService;
import com.smart.campus.utils.StringTools;


/**
 * 试卷信息表 业务接口实现
 */
@Service("paperInfoService")
public class PaperInfoServiceImpl implements PaperInfoService {

	@Resource
	private PaperInfoMapper<PaperInfo, PaperInfoQuery> paperInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<PaperInfo> findListByParam(PaperInfoQuery param) {
		return this.paperInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(PaperInfoQuery param) {
		return this.paperInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<PaperInfo> findListByPage(PaperInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<PaperInfo> list = this.findListByParam(param);
		PaginationResultVO<PaperInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(PaperInfo bean) {
		return this.paperInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<PaperInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.paperInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<PaperInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.paperInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(PaperInfo bean, PaperInfoQuery param) {
		StringTools.checkParam(param);
		return this.paperInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(PaperInfoQuery param) {
		StringTools.checkParam(param);
		return this.paperInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据PaperId获取对象
	 */
	@Override
	public PaperInfo getPaperInfoByPaperId(String paperId) {
		return this.paperInfoMapper.selectByPaperId(paperId);
	}

	/**
	 * 根据PaperId修改
	 */
	@Override
	public Integer updatePaperInfoByPaperId(PaperInfo bean, String paperId) {
		return this.paperInfoMapper.updateByPaperId(bean, paperId);
	}

	/**
	 * 根据PaperId删除
	 */
	@Override
	public Integer deletePaperInfoByPaperId(String paperId) {
		return this.paperInfoMapper.deleteByPaperId(paperId);
	}
}