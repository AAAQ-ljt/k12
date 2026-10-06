package com.nexora.admin.controller;

import com.nexora.admin.biz.PointAdminBiz;
import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.query.StudentPointRecordQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.PointRankItemVO;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端「积分运营」（二期 A-8）。
 *
 * - 学生积分总览：`/pointInfo/accountList`（按累计积分倒序，含昵称与学段）
 * - 流水审计：`/pointInfo/loadDataList`（按学生/来源/时间筛选，时间倒序取最近 N 条）
 * - 人工补分：`/pointInfo/add`（正数、必填原因，走唯一发分入口，学生端可见原因）
 *
 * 规则参数（GAME 组：等级阶梯、每日上限、各项分值）复用已有的 `/systemSetting/configList`
 * 与 `/systemSetting/config` 两个接口，不在这里再开一套。
 */
@RestController
@RequestMapping("/pointInfo")
public class PointInfoAdminController extends ABaseController {

    @Resource
    private PointAdminBiz pointAdminBiz;

    /** 学生积分总览（累计积分倒序） */
    @GetMapping("/accountList")
    public ResponseVO<List<PointRankItemVO>> accountList() {
        return getSuccessResponseVO(pointAdminBiz.accountList());
    }

    /** 积分流水审计（学生/来源/时间筛选，时间倒序） */
    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<StudentPointRecord>> loadDataList(StudentPointRecordQuery query) {
        return getSuccessResponseVO(pointAdminBiz.loadDataList(query));
    }

    /** 人工补分（补偿/活动奖励，只能加正数） */
    @PostMapping("/add")
    public ResponseVO<Integer> add(@RequestBody PointAdjustBody body, HttpServletRequest request) {
        TokenUserInfoDTO operator = (TokenUserInfoDTO) request.getAttribute(Constants.ATTR_USER_INFO);
        String operatorId = operator == null ? null : operator.getUserId();
        int granted = pointAdminBiz.addPoints(body.userId, body.stage, body.points, body.reason, operatorId);
        return getSuccessResponseVO(granted);
    }

    /** 人工补分入参（管理端内部使用，不对外暴露） */
    public static class PointAdjustBody {
        private String userId;
        private String stage;
        private Integer points;
        private String reason;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
