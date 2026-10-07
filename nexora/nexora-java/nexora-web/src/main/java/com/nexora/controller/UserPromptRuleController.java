package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.UserPromptRuleComponent;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.UserPromptRule;
import com.nexora.entity.vo.PromptRuleTypeVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.entity.vo.UserPromptRuleListVO;
import com.nexora.utils.LoginUserContext;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 学生端「AI 偏好设置」（计划 C2）：学生自定义提示词规则。
 *
 * 规则只调整回答的称呼/长短/风格/举例方式，优先级低于平台安全与事实规则；
 * 保存前服务端做长度、指令特征与黑名单三重校验（越权提示词注入防护）。
 */
@RestController
@RequestMapping("/userPromptRule")
@GlobalInterceptor(checkLogin = true)
public class UserPromptRuleController extends ABaseController {

    @Resource
    private UserPromptRuleComponent userPromptRuleComponent;

    /** 我的偏好：规则列表 + 可选类型 + 条数上限 */
    @GetMapping("/loadDataList")
    public ResponseVO<UserPromptRuleListVO> loadDataList() {
        String userId = currentUserId();
        UserPromptRuleListVO vo = new UserPromptRuleListVO();
        vo.setRules(userPromptRuleComponent.myRules(userId, null));
        List<PromptRuleTypeVO> types = new ArrayList<>();
        for (Map.Entry<String, String> entry : userPromptRuleComponent.ruleTypes().entrySet()) {
            PromptRuleTypeVO type = new PromptRuleTypeVO();
            type.setCode(entry.getKey());
            type.setName(entry.getValue());
            types.add(type);
        }
        vo.setTypes(types);
        List<UserPromptRule> rules = vo.getRules();
        int enabledCount = 0;
        for (UserPromptRule rule : rules) {
            if (rule.getStatus() != null && rule.getStatus() == 1) {
                enabledCount++;
            }
        }
        vo.setEnabledCount(enabledCount);
        vo.setEnabled(true);
        vo.setMaxCount(5);
        return getSuccessResponseVO(vo);
    }

    /** 新增一条偏好 */
    @PostMapping("/add")
    public ResponseVO<Long> add(@RequestBody PromptRuleBody body) {
        Long ruleId = userPromptRuleComponent.save(currentUserId(),
                body == null ? null : body.getRuleType(),
                body == null ? null : body.getRuleValue(),
                body == null ? null : body.getSource());
        return getSuccessResponseVO(ruleId);
    }

    /** 停用/启用一条偏好 */
    @PostMapping("/changeStatus")
    public ResponseVO<Void> changeStatus(@RequestBody PromptRuleBody body) {
        userPromptRuleComponent.changeStatus(currentUserId(),
                body == null ? null : body.getRuleId(),
                body == null ? null : body.getStatus());
        return getSuccessResponseVO(null);
    }

    /** 删除一条偏好 */
    @PostMapping("/del")
    public ResponseVO<Void> del(@RequestBody PromptRuleBody body) {
        userPromptRuleComponent.delete(currentUserId(), body == null ? null : body.getRuleId());
        return getSuccessResponseVO(null);
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return current == null ? null : current.getUserId();
    }

    /** 偏好入参 */
    public static class PromptRuleBody {
        private String ruleType;
        private String ruleValue;
        private String source;
        private Long ruleId;
        private Integer status;

        public String getRuleType() {
            return ruleType;
        }

        public void setRuleType(String ruleType) {
            this.ruleType = ruleType;
        }

        public String getRuleValue() {
            return ruleValue;
        }

        public void setRuleValue(String ruleValue) {
            this.ruleValue = ruleValue;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public Long getRuleId() {
            return ruleId;
        }

        public void setRuleId(Long ruleId) {
            this.ruleId = ruleId;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
