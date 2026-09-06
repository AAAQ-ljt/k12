package com.nexora.admin.biz;

import com.nexora.admin.dto.PracticeReviewSubmitDTO;
import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.PracticeRecord;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.query.PracticeReviewQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.PracticeReviewItemVO;
import com.nexora.entity.vo.PracticeReviewStatsVO;
import com.nexora.exception.BusinessException;
import com.nexora.mappers.PracticeReviewMapper;
import com.nexora.service.PracticeRecordService;
import com.nexora.service.QuestionInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 答题批阅业务：主观题作答列表、统计、人工打分落库。
 * 批阅分数回填 practice_record.score 供学习分析统计；不回溯改变已判定的课时通关结果。
 */
@Slf4j
@Service
public class QuestionReviewBiz {

    /** 批阅状态：0待批阅 1已批阅 */
    private static final int REVIEW_STATUS_PENDING = 0;
    private static final int REVIEW_STATUS_DONE = 1;

    /** 主观题最小题型（4简答 5解答 6论述 7材料） */
    private static final int SUBJECTIVE_MIN_TYPE = 4;

    @Resource
    private PracticeReviewMapper practiceReviewMapper;

    @Resource
    private PracticeRecordService practiceRecordService;

    @Resource
    private QuestionInfoService questionInfoService;

    public PaginationResultVO<PracticeReviewItemVO> loadList(PracticeReviewQuery query) {
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(15);
        }
        int count = practiceReviewMapper.selectReviewCount(query);
        SimplePage page = new SimplePage(query.getPageNo(), count, query.getPageSize());
        query.setSimplePage(page);
        List<PracticeReviewItemVO> list = practiceReviewMapper.selectReviewList(query);
        return new PaginationResultVO<>(count, page.getPageSize(), query.getPageNo(), page.getPageTotal(), list);
    }

    public PracticeReviewStatsVO stats() {
        return practiceReviewMapper.selectReviewStats();
    }

    /**
     * 提交批阅：校验主观题与分值范围，回填批阅信息并把得分写入 score 供学习分析统计。
     */
    @Transactional(rollbackFor = Exception.class)
    public void review(PracticeReviewSubmitDTO dto, String reviewerId) {
        if (dto == null || dto.getRecordId() == null) {
            throw new BusinessException("作答记录ID不能为空");
        }
        if (dto.getReviewScore() == null || dto.getReviewScore() < 0) {
            throw new BusinessException("请输入有效的批阅得分");
        }
        if (dto.getReviewComment() != null && dto.getReviewComment().length() > 500) {
            throw new BusinessException("批阅评语最多 500 字");
        }
        PracticeRecord record = practiceRecordService.getPracticeRecordByRecordId(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("作答记录不存在");
        }
        Integer questionType = record.getQuestionType();
        if (questionType == null || questionType < SUBJECTIVE_MIN_TYPE) {
            throw new BusinessException("仅主观题支持人工批阅");
        }
        QuestionInfo question = questionInfoService.getQuestionInfoByQuestionId(record.getQuestionId());
        int maxScore = question == null || question.getScore() == null || question.getScore() <= 0
                ? 100 : question.getScore();
        if (dto.getReviewScore() > maxScore) {
            throw new BusinessException("批阅得分不能超过题目满分 " + maxScore + " 分");
        }
        PracticeRecord update = new PracticeRecord();
        update.setReviewStatus(REVIEW_STATUS_DONE);
        update.setReviewScore(dto.getReviewScore());
        update.setScore(dto.getReviewScore());
        update.setReviewerId(StringTools.isEmpty(reviewerId) ? "admin" : reviewerId);
        update.setReviewComment(dto.getReviewComment());
        update.setReviewTime(new Date());
        practiceRecordService.updatePracticeRecordByRecordId(update, dto.getRecordId());
        log.info("答题批阅完成 recordId={} reviewer={} score={}", dto.getRecordId(), reviewerId, dto.getReviewScore());
    }
}
