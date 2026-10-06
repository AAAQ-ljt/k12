package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.CodingContestRecordVO;
import com.nexora.entity.vo.CodingContestVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.service.CodingLabBiz;
import com.nexora.utils.LoginUserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端编程比赛：本学段比赛列表、我的比赛（个人中心）、报名、进入比赛、提交成绩、排行榜。
 */
@RestController
@RequestMapping("/codingContest")
@GlobalInterceptor(checkLogin = true)
public class StudentCodingContestController extends ABaseController {

    @Resource
    private CodingLabBiz codingLabBiz;

    /** 本学段可参加的比赛（已发布且未结束） */
    @GetMapping("/list")
    public ResponseVO<List<CodingContestVO>> list() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(codingLabBiz.listContests(current.getStage(), current.getUserId()));
    }

    /** 我报名/参加过的比赛（个人中心） */
    @GetMapping("/myList")
    public ResponseVO<List<CodingContestVO>> myList() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(codingLabBiz.listMyContests(current.getUserId()));
    }

    @GetMapping("/getInfo")
    public ResponseVO<CodingContestVO> getInfo(@RequestParam String contestId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(codingLabBiz.getContestDetail(contestId, current.getUserId(), current.getStage()));
    }

    @PostMapping("/enroll")
    public ResponseVO<Void> enroll(@RequestParam String contestId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        codingLabBiz.enroll(contestId, current.getUserId(), current.getStage());
        return getSuccessResponseVO(null);
    }

    @PostMapping("/start")
    public ResponseVO<CodingContestVO> start(@RequestParam String contestId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(codingLabBiz.start(contestId, current.getUserId(), current.getStage()));
    }

    @PostMapping("/submit")
    public ResponseVO<Void> submit(@RequestParam String contestId,
                                   @RequestParam(required = false) Integer score,
                                   @RequestParam(required = false) Integer solvedCount,
                                   @RequestParam(required = false) Integer duration) {
        TokenUserInfoDTO current = LoginUserContext.get();
        codingLabBiz.submit(contestId, current.getUserId(), score, solvedCount, duration);
        return getSuccessResponseVO(null);
    }

    @GetMapping("/rank")
    public ResponseVO<List<CodingContestRecordVO>> rank(@RequestParam String contestId,
                                                        @RequestParam(required = false) Integer limit) {
        TokenUserInfoDTO current = LoginUserContext.get();
        List<CodingContestRecordVO> list = codingLabBiz.rank(contestId, current.getUserId(), limit);
        return getSuccessResponseVO(codingLabBiz.markMine(list, current.getUserId()));
    }
}
