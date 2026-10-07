package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.PreferencePageComponent;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.PreferencePageVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.utils.LoginUserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 《我的学习偏好》系统页接口（计划 C3）。
 *
 * 说明：这是一篇真正的知识页（在学生个人知识库里、可读可改），这里只提供**系统页特有**的三个动作：
 * 打开（不存在则自动创建并绑定锚点）、保存（折叠渲染：只取自由段，规则段仍由系统生成）、重置。
 */
@RestController
@RequestMapping("/preferencePage")
@GlobalInterceptor(checkLogin = true)
public class PreferencePageController extends ABaseController {

    @Resource
    private PreferencePageComponent preferencePageComponent;

    /** 打开偏好页（首次访问自动创建，返回 docId 供知识页编辑器直接使用） */
    @GetMapping("/getInfo")
    public ResponseVO<PreferencePageVO> getInfo() {
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? null : current.getUserId();
        String stage = current == null ? null : current.getStage();
        return getSuccessResponseVO(preferencePageComponent.page(userId, stage));
    }

    /** 保存正文（只取自由段，规则段由系统重新渲染） */
    @PostMapping("/save")
    public ResponseVO<PreferencePageVO> save(@RequestBody PreferencePageBody body) {
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? null : current.getUserId();
        String stage = current == null ? null : current.getStage();
        PreferencePageVO page = preferencePageComponent.page(userId, stage);
        if (page == null) {
            return getSuccessResponseVO(null);
        }
        preferencePageComponent.saveFromEditor(userId, page.getDocId(), body == null ? null : body.getContent());
        return getSuccessResponseVO(preferencePageComponent.page(userId, stage));
    }

    /** 重置：清空学生自填规则（保留系统默认）并把正文恢复成初始内容 */
    @PostMapping("/reset")
    public ResponseVO<PreferencePageVO> reset() {
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? null : current.getUserId();
        String stage = current == null ? null : current.getStage();
        return getSuccessResponseVO(preferencePageComponent.reset(userId, stage));
    }

    /** 保存入参 */
    public static class PreferencePageBody {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
