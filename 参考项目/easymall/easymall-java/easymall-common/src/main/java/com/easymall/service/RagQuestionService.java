package com.easymall.service;

import com.easymall.entity.po.RagQuestion;
import com.easymall.entity.query.RagQuestionQuery;
import com.easymall.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * rag问题 业务接口
 */
public interface RagQuestionService {

    /**
     * 根据条件查询列表
     */
    List<RagQuestion> findListByParam(RagQuestionQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(RagQuestionQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<RagQuestion> findListByPage(RagQuestionQuery param);

    /**
     * 新增
     */
    Integer add(RagQuestion bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<RagQuestion> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<RagQuestion> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(RagQuestion bean, RagQuestionQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(RagQuestionQuery param);

    /**
     * 根据QuestionId查询对象
     */
    RagQuestion getRagQuestionByQuestionId(Integer questionId);


    /**
     * 根据QuestionId修改
     */
    Integer updateRagQuestionByQuestionId(RagQuestion bean, Integer questionId);


    /**
     * 根据QuestionId删除
     */
    Integer deleteRagQuestionByQuestionId(Integer questionId);

    void saveRagQuestion(RagQuestion ragQuestion);

    void delRagQuestion(Integer questionId);
}