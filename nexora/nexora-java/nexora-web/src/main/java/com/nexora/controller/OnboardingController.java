package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.OnboardingComponent;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.OnboardingStatusVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.utils.LoginUserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端新手引导（计划 D1）。
 *
 * 前端在页面加载时取一次状态：首次 → 自动弹欢迎卡；老用户 → 只在右上角「新手指引」里看进度与「有更新」提示。
 * 步骤文案与高亮目标都在前端配置里（按学段筛），后端只存进度，改文案不用发版后端。
 */
@RestController
@RequestMapping("/onboarding")
@GlobalInterceptor(checkLogin = true)
public class OnboardingController extends ABaseController {

    @Resource
    private OnboardingComponent onboardingComponent;

    /** 我的引导状态（是否首次 / 已完成步骤 / 是否有更新） */
    @GetMapping("/status")
    public ResponseVO<OnboardingStatusVO> status() {
        return getSuccessResponseVO(onboardingComponent.status(currentUserId()));
    }

    /** 记录看过欢迎卡 / 选择先自己看看 */
    @PostMapping("/welcome")
    public ResponseVO<Void> welcome(@RequestBody OnboardingBody body) {
        onboardingComponent.recordWelcome(currentUserId(), currentStage(),
                body != null && Boolean.TRUE.equals(body.getWelcomeSeen()),
                body != null && Boolean.TRUE.equals(body.getSkipped()));
        return getSuccessResponseVO(null);
    }

    /** 批量上报引导进度 */
    @PostMapping("/step")
    public ResponseVO<Void> step(@RequestBody OnboardingBody body) {
        onboardingComponent.recordSteps(currentUserId(), currentStage(),
                body == null ? null : body.getSteps(),
                body != null && Boolean.TRUE.equals(body.getFinished()));
        return getSuccessResponseVO(null);
    }

    /** 重看导览（清空已完成步骤） */
    @PostMapping("/reset")
    public ResponseVO<Void> reset() {
        onboardingComponent.reset(currentUserId(), currentStage());
        return getSuccessResponseVO(null);
    }

    /** 打开引导中心（只记时间，便于运营看到使用情况） */
    @PostMapping("/open")
    public ResponseVO<Void> open() {
        onboardingComponent.touchOpen(currentUserId(), currentStage());
        return getSuccessResponseVO(null);
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return current == null ? null : current.getUserId();
    }

    private String currentStage() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return current == null ? null : current.getStage();
    }

    /** 入参 */
    public static class OnboardingBody {
        private Boolean welcomeSeen;
        private Boolean skipped;
        private List<String> steps;
        private Boolean finished;

        public Boolean getWelcomeSeen() {
            return welcomeSeen;
        }

        public void setWelcomeSeen(Boolean welcomeSeen) {
            this.welcomeSeen = welcomeSeen;
        }

        public Boolean getSkipped() {
            return skipped;
        }

        public void setSkipped(Boolean skipped) {
            this.skipped = skipped;
        }

        public List<String> getSteps() {
            return steps;
        }

        public void setSteps(List<String> steps) {
            this.steps = steps;
        }

        public Boolean getFinished() {
            return finished;
        }

        public void setFinished(Boolean finished) {
            this.finished = finished;
        }
    }
}
