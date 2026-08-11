package com.easymall.controller;

import com.easymall.component.RedisComponent;
import com.easymall.entity.dto.LogisticsSendDTO;
import com.easymall.entity.enums.PromptTypeEnum;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/setting")
@Validated
public class SettingController extends ABaseController {

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/saveLogistics")
    public ResponseVO saveLogistics(LogisticsSendDTO logisticsSendDTO) {
        redisComponent.saveLogisticsInfo(logisticsSendDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getLogistics")
    public ResponseVO getLogistics() {
        return getSuccessResponseVO(redisComponent.getLogisticsInfo());
    }

    @RequestMapping("/loadPromptList")
    public ResponseVO loadPromptList() {
        return getSuccessResponseVO(PromptTypeEnum.getPrompts());
    }

    @RequestMapping("/getPromptDetail")
    public ResponseVO getPromptDetail(String key) {
        String prompt = redisComponent.getPrompt(key);
        if (StringTools.isEmpty(prompt)) {
            prompt = PromptTypeEnum.getByKey(key).getPrompt();
        }
        return getSuccessResponseVO(prompt);
    }

    @RequestMapping("/savePrompt")
    public ResponseVO savePrompt(String key, String prompt) {
        redisComponent.savePrompt(key, prompt);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/cleanPromptCache")
    public ResponseVO cleanPromptCache(String key) {
        redisComponent.cleanPrompt(key);
        return getSuccessResponseVO(null);
    }
}
