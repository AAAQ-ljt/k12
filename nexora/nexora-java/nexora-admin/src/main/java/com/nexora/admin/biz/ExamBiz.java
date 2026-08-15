package com.nexora.admin.biz;

import com.nexora.constants.Constants;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.ExamInfo;
import com.nexora.entity.query.ExamInfoQuery;
import com.nexora.entity.vo.ExamInfoVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ExamInfoService;
import com.nexora.service.PaperInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 考试管理业务
 */
@Service
public class ExamBiz {

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private PaperInfoService paperInfoService;

    public PaginationResultVO<ExamInfoVO> page(ExamInfoQuery query) {
        if (StringTools.isEmpty(query.getOrderBy())) {
            query.setOrderBy("create_time desc");
        }
        return examInfoService.findListByPage(query);
    }

    public ExamInfoVO detail(String examId) {
        if (StringTools.isEmpty(examId)) {
            throw new BusinessException("考试ID不能为空");
        }
        ExamInfoVO exam = examInfoService.getById(examId);
        if (exam == null) {
            throw new BusinessException("考试不存在");
        }
        return exam;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(ExamInfo bean) {
        if (bean == null) {
            throw new BusinessException("考试信息不能为空");
        }
        if (StringTools.isEmpty(bean.getExamName())) {
            throw new BusinessException("请填写考试名称");
        }
        if (StringTools.isEmpty(bean.getGrade())) {
            throw new BusinessException("请选择年级");
        }
        if (StringTools.isEmpty(bean.getPaperId())) {
            throw new BusinessException("请选择试卷");
        }
        if (paperInfoService.getById(bean.getPaperId()) == null) {
            throw new BusinessException("试卷不存在");
        }
        String stage = StageEnum.matchByGrade(bean.getGrade());
        if (stage == null) {
            throw new BusinessException("非法的年级");
        }
        bean.setStage(stage);
        Date now = new Date();
        if (StringTools.isEmpty(bean.getExamId())) {
            bean.setExamId(StringTools.getRandomNumber(Constants.LENGTH_15));
            if (bean.getStatus() == null) {
                bean.setStatus(0);
            }
            if (bean.getDurationMinutes() == null) {
                bean.setDurationMinutes(60);
            }
            bean.setCreateTime(now);
            bean.setUpdateTime(now);
            examInfoService.insert(bean);
        } else {
            if (examInfoService.getById(bean.getExamId()) == null) {
                throw new BusinessException("考试不存在");
            }
            bean.setUpdateTime(now);
            examInfoService.update(bean);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String examId) {
        if (StringTools.isEmpty(examId)) {
            throw new BusinessException("考试ID不能为空");
        }
        if (examInfoService.getById(examId) == null) {
            throw new BusinessException("考试不存在");
        }
        examInfoService.deleteById(examId);
    }
}
