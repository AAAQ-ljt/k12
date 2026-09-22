package com.nexora.admin.controller;

import com.nexora.admin.biz.SystemSettingBiz;
import com.nexora.admin.dto.PromptSaveDTO;
import com.nexora.admin.dto.RagConfigSaveDTO;
import com.nexora.admin.vo.PromptEffectiveVO;
import com.nexora.admin.vo.RagConfigItemVO;
import com.nexora.admin.vo.RuntimeInfoVO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 系统设置：环境配置 + 模型与提示词 + RAG 配置 + 模型验证入口
 *
 * 分工：
 * - `/configList`、`/promptList`、`/config`、`/prompt` 为既有通用读写（保留兼容）；
 * - `/ragConfig`（读写）为本期新增的可调参数，**运行时真生效**；
 * - `/runtimeInfo` 为只读脱敏的环境与模型信息；
 * - `/promptEffective`、`/promptSave` 展示与保存提示词三层覆盖（Redis → 表 → 枚举默认）。
 */
@RestController
@RequestMapping("/systemSetting")
public class SystemSettingController extends ABaseController {

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private PromptTemplateService promptTemplateService;

    @Resource
    private SystemSettingBiz systemSettingBiz;

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

    /** RAG 可调参数列表（当前值 / 默认值 / 范围） */
    @GetMapping("/ragConfig")
    public ResponseVO<List<RagConfigItemVO>> ragConfigList() {
        return getSuccessResponseVO(systemSettingBiz.ragConfigList());
    }

    /** 保存单个 RAG 参数（白名单 + 类型 + 范围校验，保存即生效） */
    @PostMapping("/ragConfig")
    public ResponseVO<Void> saveRagConfig(@RequestBody RagConfigSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException("参数不能为空");
        }
        systemSettingBiz.saveRagConfig(dto.getConfigKey(), dto.getConfigValue());
        return getSuccessResponseVO(null);
    }

    /** 运行时环境与模型信息（只读，Key 已掩码） */
    @GetMapping("/runtimeInfo")
    public ResponseVO<RuntimeInfoVO> runtimeInfo() {
        return getSuccessResponseVO(systemSettingBiz.runtimeInfo());
    }

    /** 各场景提示词生效情况（默认 ALL 学段，可指定学段） */
    @GetMapping("/promptEffective")
    public ResponseVO<List<PromptEffectiveVO>> promptEffective(
            @RequestParam(required = false) String stage) {
        return getSuccessResponseVO(systemSettingBiz.promptEffective(stage));
    }

    /** 保存提示词覆盖（按 stage + scene upsert，写库即生效并清除 Redis 覆盖） */
    @PostMapping("/promptSave")
    public ResponseVO<Void> savePrompt(@RequestBody PromptSaveDTO dto) {
        if (dto == null) {
            throw new BusinessException("参数不能为空");
        }
        systemSettingBiz.savePrompt(dto.getStage(), dto.getScene(), dto.getContent(), dto.getStatus());
        return getSuccessResponseVO(null);
    }
}

