package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.dto.StudentWikiProfileDTO;
import com.nexora.dto.StudentWikiUpdateDraftDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.UserWikiProfile;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.StudentWikiService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生个人知识页（wiki 层）Controller：草稿生成 / 编辑 / 确认 / 列表 / 学习档案
 */
@RestController
@RequestMapping("/studentWiki")
@GlobalInterceptor(checkLogin = true)
public class StudentWikiController extends ABaseController {

    @Resource
    private StudentWikiService studentWikiService;

    @PostMapping("/generate")
    public ResponseVO<KnowledgeDoc> generate(@RequestParam String resourceId) {
        return getSuccessResponseVO(studentWikiService.generateDraft(currentUserId(), resourceId));
    }

    /**
     * 同步 AI 对话消息为知识页草稿（L3）
     */
    @PostMapping("/syncFromMessage")
    public ResponseVO<KnowledgeDoc> syncFromMessage(@RequestParam String messageId) {
        return getSuccessResponseVO(studentWikiService.syncFromMessage(currentUserId(), messageId));
    }

    @PutMapping("/draft")
    public ResponseVO<KnowledgeDoc> updateDraft(@RequestBody StudentWikiUpdateDraftDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getDocId())) {
            throw new BusinessException("参数错误");
        }
        return getSuccessResponseVO(studentWikiService.updateDraft(currentUserId(), dto.getDocId(), dto.getContent()));
    }

    @PostMapping("/confirm")
    public ResponseVO<KnowledgeDoc> confirm(@RequestParam String docId) {
        return getSuccessResponseVO(studentWikiService.confirm(currentUserId(), docId));
    }

    @GetMapping("/getInfo")
    public ResponseVO<KnowledgeDoc> getInfo(@RequestParam String docId) {
        return getSuccessResponseVO(studentWikiService.getDraft(currentUserId(), docId));
    }

    @GetMapping("/list")
    public ResponseVO<List<KnowledgeDoc>> list(@RequestParam(required = false) String resourceId) {
        return getSuccessResponseVO(studentWikiService.listDrafts(currentUserId(), resourceId));
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String docId) {
        studentWikiService.deleteDraft(currentUserId(), docId);
        return getSuccessResponseVO(null);
    }

    @GetMapping("/profile")
    public ResponseVO<UserWikiProfile> profile() {
        return getSuccessResponseVO(studentWikiService.getProfile(currentUserId()));
    }

    @PutMapping("/profile")
    public ResponseVO<UserWikiProfile> saveProfile(@RequestBody StudentWikiProfileDTO dto) {
        if (dto == null) {
            throw new BusinessException("参数错误");
        }
        return getSuccessResponseVO(studentWikiService.saveProfile(currentUserId(), dto));
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }
}