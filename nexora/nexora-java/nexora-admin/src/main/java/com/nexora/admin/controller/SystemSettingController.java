package com.nexora.admin.controller;

import com.nexora.controller.ABaseController;
import com.nexora.entity.po.PromptTemplate;
import com.nexora.entity.po.SystemConfig;
import com.nexora.entity.query.PromptTemplateQuery;
import com.nexora.entity.query.SystemConfigQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.PromptTemplateService;
import com.nexora.service.SystemConfigService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 系统设置：环境配置 + 模型/提示词
 */
@RestController
@RequestMapping("/systemSetting")
public class SystemSettingController extends ABaseController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private PromptTemplateService promptTemplateService;

    @GetMapping("/configList")
    public ResponseVO<List<SystemConfig>> configList() {
        SystemConfigQuery query = new SystemConfigQuery();
        query.setOrderBy("config_group asc, config_key asc");
        return getSuccessResponseVO(systemConfigService.findListByParam(query));
    }

    @PutMapping("/config")
    public ResponseVO<Void> updateConfig(@RequestBody SystemConfig bean) {
        if (bean.getConfigId() == null) {
            throw new BusinessException("配置ID不能为空");
        }
        if (bean.getConfigValue() == null) {
            throw new BusinessException("配置值不能为空");
        }
        SystemConfig update = new SystemConfig();
        update.setConfigValue(bean.getConfigValue());
        update.setDescription(bean.getDescription());
        update.setStatus(bean.getStatus());
        update.setUpdateTime(new Date());
        systemConfigService.updateSystemConfigByConfigId(update, bean.getConfigId());
        return getSuccessResponseVO(null);
    }

    @GetMapping("/promptList")
    public ResponseVO<List<PromptTemplate>> promptList() {
        PromptTemplateQuery query = new PromptTemplateQuery();
        query.setOrderBy("stage asc, scene asc");
        return getSuccessResponseVO(promptTemplateService.findListByParam(query));
    }

    @PutMapping("/prompt")
    public ResponseVO<Void> updatePrompt(@RequestBody PromptTemplate bean) {
        if (bean.getId() == null) {
            throw new BusinessException("提示词ID不能为空");
        }
        if (StringTools.isEmpty(bean.getContent())) {
            throw new BusinessException("提示词内容不能为空");
        }
        PromptTemplate update = new PromptTemplate();
        update.setTemplateName(bean.getTemplateName());
        update.setContent(bean.getContent());
        update.setStatus(bean.getStatus());
        update.setRemark(bean.getRemark());
        update.setUpdateTime(new Date());
        promptTemplateService.updatePromptTemplateById(update, bean.getId());
        return getSuccessResponseVO(null);
    }
}
