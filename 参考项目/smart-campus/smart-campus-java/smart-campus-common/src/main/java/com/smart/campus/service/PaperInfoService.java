package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.PaperInfoQuery;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 试卷信息表 业务接口
 */
public interface PaperInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<PaperInfo> findListByParam(PaperInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(PaperInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<PaperInfo> findListByPage(PaperInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(PaperInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<PaperInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<PaperInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(PaperInfo bean,PaperInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(PaperInfoQuery param);

	/**
	 * 根据PaperId查询对象
	 */
	PaperInfo getPaperInfoByPaperId(String paperId);


	/**
	 * 根据PaperId修改
	 */
	Integer updatePaperInfoByPaperId(PaperInfo bean,String paperId);


	/**
	 * 根据PaperId删除
	 */
	Integer deletePaperInfoByPaperId(String paperId);

}