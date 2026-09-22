package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.entity.enums.PromptTypeEnum;
import com.nexora.entity.enums.StageEnum;
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
            return substituteStageDesc(redisValue, stage);
        }
        String dbValue = findDbTemplate(stage, scene);
        if (dbValue != null) {
            return substituteStageDesc(dbValue, stage);
        }
        PromptTypeEnum promptType = PromptTypeEnum.getByScene(scene);
        if (promptType == null) {
            return PromptTypeEnum.CHAT.getDefaultPrompt(stage);
        }
        return promptType.getDefaultPrompt(stage);
    }

    /**
     * 数据库 / Redis 维护的模板同样支持 {stageDesc} 占位符（与枚举默认值口径一致，避免管理端编辑后占位符原样下发）
     */
    private String substituteStageDesc(String template, String stage) {
        if (template == null || !template.contains("{stageDesc}")) {
            return template;
        }
        return template.replace("{stageDesc}", stageDesc(stage));
    }

    private String stageDesc(String stage) {
        if (stage == null) {
            return "未知学段";
        }
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equals(stage)) {
                return item.getDesc();
            }
        }
        return "未知学段";
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
