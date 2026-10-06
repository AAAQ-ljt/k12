package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.vo.PointAccountVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.service.PointBiz;
import com.nexora.utils.LoginUserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端积分（二期·积分游戏化 A-1/A-2）。
 *
 * 只读接口；写入全部在服务端由 {@code PointAwardComponent} 按学习事件结算，前端不可直接加分。
 * 说明：编程比赛的排名与成绩是独立体系，不产生全局积分，因此这里查不到比赛相关流水（见规划 A-5）。
 */
@RestController
@RequestMapping("/pointInfo")
@GlobalInterceptor(checkLogin = true)
public class PointInfoController extends ABaseController {

    @Resource
    private PointBiz pointBiz;

    /** 我的积分账户：累计/可用积分、等级与进度、连续天数、今日已得与上限 */
    @GetMapping("/getMyAccount")
    public ResponseVO<PointAccountVO> getMyAccount() {
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? null : current.getUserId();
        return getSuccessResponseVO(pointBiz.myAccount(userId));
    }

    /** 我的积分明细（按时间倒序） */
    @GetMapping("/loadDataList")
    public ResponseVO<List<StudentPointRecord>> loadDataList(
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) String createTimeStart,
            @RequestParam(required = false) String createTimeEnd,
            @RequestParam(required = false) Integer pageSize) {
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? null : current.getUserId();
        return getSuccessResponseVO(pointBiz.myRecords(userId, bizType, createTimeStart, createTimeEnd, pageSize));
    }
}
