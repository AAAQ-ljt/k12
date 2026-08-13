package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.enums.PromptTypeEnum;
import com.nexora.entity.po.PromptTemplate;
import com.nexora.entity.query.PromptTemplateQuery;
import com.nexora.service.PromptTemplateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 提示词解析：Redis 覆盖 -> prompt_template 表 -> 枚举默认值
 */
@Component
public class PromptTemplateComponent {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private PromptTemplateService promptTemplateService;

    public String resolvePrompt(String stage, String scene) {
        String redisValue = redisComponent.getString(Constants.REDIS_KEY_PROMPT_TEMPLATE + stage + ":" + scene);
        if (redisValue != null && !redisValue.isBlank()) {
            return redisValue;
        }
        String dbValue = findDbTemplate(stage, scene);
        if (dbValue != null) {
            return dbValue;
        }
        PromptTypeEnum promptType = PromptTypeEnum.getByScene(scene);
        if (promptType == null) {
            return PromptTypeEnum.CHAT.getDefaultPrompt(stage);
        }
        return promptType.getDefaultPrompt(stage);
    }

    private String findDbTemplate(String stage, String scene) {
        String value = queryDb(stage, scene);
        if (value == null) {
            value = queryDb("ALL", scene);
        }
        return value;
    }

    private String queryDb(String stage, String scene) {
        PromptTemplateQuery query = new PromptTemplateQuery();
        query.setStage(stage);
        query.setScene(scene);
        query.setStatus(Constants.STATUS_ENABLE);
        query.setOrderBy("id asc");
        List<PromptTemplate> list = promptTemplateService.findListByParam(query);
        if (list == null || list.isEmpty() || list.get(0).getContent() == null) {
            return null;
        }
        String content = list.get(0).getContent().trim();
        return content.isEmpty() ? null : content;
    }
}
