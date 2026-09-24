package com.nexora.admin.biz;

import com.nexora.admin.vo.ImageProviderOptionsVO;
import com.nexora.admin.vo.PromptEffectiveVO;
import com.nexora.admin.vo.RagConfigItemVO;
import com.nexora.admin.vo.RuntimeInfoVO;
import com.nexora.admin.vo.RuntimeItemVO;
import com.nexora.component.ImageProviderRouter;
import com.nexora.component.RedisComponent;
import com.nexora.component.SystemConfigComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.enums.PromptTypeEnum;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.PromptTemplate;
import com.nexora.entity.po.SystemConfig;
import com.nexora.entity.query.PromptTemplateQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.PromptTemplateService;
import com.nexora.service.SystemConfigService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置业务：RAG 可调参数（真生效）、运行时环境与模型信息（只读脱敏）、提示词三层生效情况。
 *
 * 说明：
 * - RAG 参数是本期内唯一"管理端可改且运行时真读取"的配置（见 {@link SystemConfigComponent}）；
 * - 模型与环境配置一律只读展示（Key/base-url 属环境变量），改 env 后需重启服务；
 * - 提示词保存写 prompt_template 表即生效（解析顺序 Redis → 表 → 枚举默认），保存时顺带清掉该场景的 Redis 覆盖，
 *   避免旧覆盖压住新内容。
 */
@Slf4j
@Service
public class SystemSettingBiz {

    /** 提示词生效来源 */
    private static final String SOURCE_ENUM = "ENUM_DEFAULT";
    private static final String SOURCE_DB = "DB";
    private static final String SOURCE_REDIS = "REDIS";

    /** 提示词内容长度上限 */
    private static final int MAX_PROMPT_LENGTH = 20000;

    @Resource
    private SystemConfigComponent systemConfigComponent;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private PromptTemplateService promptTemplateService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ImageProviderRouter imageProviderRouter;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${server.port:6061}")
    private String serverPort;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private String redisPort;

    @Value("${spring.data.redis.database:0}")
    private String redisDatabase;

    @Value("${spring.elasticsearch.uris:}")
    private String esUris;

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${spring.ai.openai.chat.options.model:}")
    private String chatModel;

    @Value("${spring.ai.openai.chat.base-url:}")
    private String chatBaseUrl;

    @Value("${spring.ai.openai.chat.api-key:}")
    private String chatApiKey;

    @Value("${project.ai.vision-model:}")
    private String visionModel;

    @Value("${spring.ai.openai.embedding.options.model:}")
    private String embeddingModel;

    @Value("${spring.ai.openai.embedding.options.dimensions:}")
    private String embeddingDimensions;

    @Value("${spring.ai.openai.embedding.base-url:}")
    private String embeddingBaseUrl;

    @Value("${spring.ai.openai.embedding.api-key:${spring.ai.openai.api-key:}}")
    private String embeddingApiKey;

    @Value("${spring.ai.vectorstore.elasticsearch.index-name:}")
    private String vectorIndexName;

    @Value("${project.ai.image.model:}")
    private String imageModel;

    @Value("${project.ai.image.ark-model:}")
    private String imageArkModel;

    @Value("${project.ai.image.base-url:}")
    private String imageBaseUrl;

    @Value("${project.ai.image.api-key:}")
    private String imageApiKey;

    @Value("${project.ai.image.ark-api-key:}")
    private String imageArkApiKey;

    @Value("${project.ai.image.ark-base-url:}")
    private String imageArkBaseUrl;

    @Value("${project.ai.image.gpt-model:}")
    private String imageGptModel;

    @Value("${project.ai.image.gpt-base-url:}")
    private String imageGptBaseUrl;

    @Value("${project.ai.image.gpt-api-key:}")
    private String imageGptApiKey;

    // ==================== RAG 配置 ====================

    /**
     * RAG 可调参数列表：登记的定义 + 数据库覆盖值（未定制时 currentValue = 默认值）
     */
    public List<RagConfigItemVO> ragConfigList() {
        List<RagConfigItemVO> list = new ArrayList<>();
        for (SystemConfigComponent.ConfigDefinition definition : systemConfigComponent.listDefinitions()) {
            RagConfigItemVO vo = new RagConfigItemVO();
            vo.setConfigKey(definition.configKey());
            vo.setConfigName(definition.configName());
            vo.setConfigType(definition.configType());
            vo.setDescription(definition.description());
            vo.setDefaultValue(definition.configValue());
            vo.setMinValue(definition.minValue());
            vo.setMaxValue(definition.maxValue());
            SystemConfig config = systemConfigService.getSystemConfigByConfigGroupAndConfigKey(
                    definition.configGroup(), definition.configKey());
            boolean customized = config != null && !StringTools.isEmpty(config.getConfigValue());
            vo.setCustomized(customized);
            vo.setCurrentValue(customized ? config.getConfigValue().trim() : definition.configValue());
            list.add(vo);
        }
        return list;
    }

    /**
     * 保存单个 RAG 参数（白名单 + 类型 + 范围校验），保存后运行时立即生效
     */
    public void saveRagConfig(String configKey, String configValue) {
        SystemConfigComponent.ConfigDefinition definition = systemConfigComponent.findDefinition(configKey);
        if (definition == null) {
            throw new BusinessException("不支持的配置项：" + configKey);
        }
        if (StringTools.isEmpty(configValue)) {
            throw new BusinessException("配置值不能为空");
        }
        String value = configValue.trim();
        validateRagValue(definition, value);
        SystemConfig existing = systemConfigService.getSystemConfigByConfigGroupAndConfigKey(
                definition.configGroup(), definition.configKey());
        Date now = new Date();
        if (existing == null) {
            SystemConfig config = new SystemConfig();
            config.setConfigGroup(definition.configGroup());
            config.setConfigKey(definition.configKey());
            config.setConfigValue(value);
            config.setConfigType(definition.configType());
            config.setDescription(definition.description());
            config.setStatus(1);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            systemConfigService.add(config);
        } else {
            SystemConfig update = new SystemConfig();
            update.setConfigValue(value);
            update.setUpdateTime(now);
            systemConfigService.updateSystemConfigByConfigId(update, existing.getConfigId());
        }
        log.info("RAG 配置已更新 key={} value={}", configKey, value);
    }

    private void validateRagValue(SystemConfigComponent.ConfigDefinition definition, String value) {
        try {
            if (SystemConfigComponent.TYPE_FLOAT.equals(definition.configType())) {
                double parsed = Double.parseDouble(value);
                if (SystemConfigComponent.KEY_RAG_SIMILARITY_THRESHOLD.equals(definition.configKey())
                        && (parsed <= 0 || parsed > 1)) {
                    throw new BusinessException("相似度阈值需在 0-1 之间");
                }
                return;
            }
            int parsed = Integer.parseInt(value);
            if (definition.minValue() != null && parsed < definition.minValue()) {
                throw new BusinessException(definition.configName() + "不能小于 " + definition.minValue());
            }
            if (definition.maxValue() != null && parsed > definition.maxValue()) {
                throw new BusinessException(definition.configName() + "不能大于 " + definition.maxValue());
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(definition.configName() + "必须是数字");
        }
    }

    // ==================== 运行时环境与模型（只读脱敏） ====================

    public RuntimeInfoVO runtimeInfo() {
        RuntimeInfoVO vo = new RuntimeInfoVO();
        vo.setProfile(activeProfile);
        vo.setServerPort(serverPort);

        List<RuntimeItemVO> infrastructure = new ArrayList<>();
        infrastructure.add(new RuntimeItemVO("MySQL", StringTools.isEmpty(datasourceUrl) ? "（未配置）" : datasourceUrl,
                "连接地址与库名；账号密码不展示"));
        infrastructure.add(new RuntimeItemVO("Redis",
                redisHost + ":" + redisPort + "（database " + redisDatabase + "）", "登录态 / 队列 / 任务"));
        infrastructure.add(new RuntimeItemVO("Elasticsearch", StringTools.isEmpty(esUris) ? "（未配置）" : esUris,
                "向量检索；存活状态见「模型验证」与知识库检索测试"));
        infrastructure.add(new RuntimeItemVO("向量索引", vectorIndexName, "ES 向量索引名（维度与向量模型一致）"));
        infrastructure.add(new RuntimeItemVO("项目目录", projectFolder, "资源文件落盘根目录"));
        vo.setInfrastructure(infrastructure);

        List<RuntimeItemVO> models = new ArrayList<>();
        models.add(new RuntimeItemVO("对话模型", chatModel, "DeepSeek；env NEXORA_DEEPSEEK_API_KEY"));
        models.add(new RuntimeItemVO("对话 base-url", chatBaseUrl, "改动需重启服务"));
        models.add(new RuntimeItemVO("对话 Key", maskKey(chatApiKey), "只读展示（已掩码）"));
        models.add(new RuntimeItemVO("视觉模型", visionModel, "带图对话使用；env NEXORA_VISION_MODEL"));
        models.add(new RuntimeItemVO("向量模型", embeddingModel + "（" + embeddingDimensions + " 维）",
                "阿里百炼；env NEXORA_DASHSCOPE_API_KEY / NEXORA_EMBEDDING_API_KEY"));
        models.add(new RuntimeItemVO("向量 base-url", embeddingBaseUrl, "改动需重启服务"));
        models.add(new RuntimeItemVO("向量 Key", maskKey(embeddingApiKey), "只读展示（已掩码）"));
        String effectiveProvider = imageProviderRouter.currentCode();
        boolean ark = SystemConfigComponent.PROVIDER_ARK.equals(effectiveProvider);
        boolean gpt = SystemConfigComponent.PROVIDER_GPT_IMAGE_2.equals(effectiveProvider);
        String providerLabel = gpt ? "gpt-image-2（img.zikl.dev 网关）"
                : ark ? "ark（火山方舟/豆包）" : "dashscope（阿里百炼）";
        String effectiveImageModel = gpt ? imageGptModel : ark ? imageArkModel : imageModel;
        String effectiveImageBaseUrl = gpt ? imageGptBaseUrl : ark ? imageArkBaseUrl : imageBaseUrl;
        String effectiveImageKey = gpt ? imageGptApiKey : ark ? imageArkApiKey : imageApiKey;
        String effectiveImageKeyEnv = gpt ? "NEXORA_GPT_IMAGE_API_KEY"
                : ark ? "NEXORA_ARK_API_KEY" : "NEXORA_DASHSCOPE_API_KEY";
        models.add(new RuntimeItemVO("文生图供应商", providerLabel,
                "「文生图供应商」卡片可切换且保存即生效；表里无覆盖时跟随 env NEXORA_IMAGE_PROVIDER"));
        models.add(new RuntimeItemVO("文生图模型", effectiveImageModel,
                "env " + effectiveImageKeyEnv + " 对应的模型"));
        models.add(new RuntimeItemVO("文生图 base-url", effectiveImageBaseUrl, "地址随供应商变化"));
        models.add(new RuntimeItemVO("文生图 Key", maskKey(effectiveImageKey),
                "只读展示（已掩码）；env " + effectiveImageKeyEnv));
        vo.setModels(models);
        return vo;
    }

    /**
     * Key 掩码：仅保留前 6 位与后 4 位；未配置（含 sk-xxx / ark-xxx 占位）统一显示「未配置」
     */
    private String maskKey(String key) {
        if (StringTools.isEmpty(key) || key.contains("xxx")) {
            return "（未配置）";
        }
        String value = key.trim();
        if (value.length() <= 12) {
            return "****";
        }
        return value.substring(0, 6) + "****" + value.substring(value.length() - 4);
    }

    // ==================== 文生图供应商切换 ====================

    /**
     * 文生图供应商选项：当前生效值 + 可选项列表（管理端「环境配置」切换控件）
     */
    public ImageProviderOptionsVO imageProviderOptions() {
        ImageProviderOptionsVO vo = new ImageProviderOptionsVO();
        vo.setCurrent(imageProviderRouter.currentCode());
        List<ImageProviderOptionsVO.Option> options = new ArrayList<>();
        options.add(new ImageProviderOptionsVO.Option(
                SystemConfigComponent.PROVIDER_DASHSCOPE, "阿里百炼（dashscope）",
                "qwen-image 系列；env NEXORA_DASHSCOPE_API_KEY"));
        options.add(new ImageProviderOptionsVO.Option(
                SystemConfigComponent.PROVIDER_ARK, "豆包方舟（ark）",
                "doubao-seedream 系列；env NEXORA_ARK_API_KEY"));
        options.add(new ImageProviderOptionsVO.Option(
                SystemConfigComponent.PROVIDER_GPT_IMAGE_2, "gpt-image-2",
                "img.zikl.dev OpenAI 兼容网关；env NEXORA_GPT_IMAGE_API_KEY"));
        vo.setOptions(options);
        return vo;
    }

    /**
     * 切换文生图供应商（白名单校验，写 system_config 保存即生效；
     * 删除该行恢复默认时不传 null，前端恢复默认请切换回 env 默认值编码）
     */
    public void switchImageProvider(String provider) {
        if (!SystemConfigComponent.isSupportedImageProvider(provider)) {
            throw new BusinessException("不支持的文生图供应商：" + provider);
        }
        String value = provider.trim();
        SystemConfig existing = systemConfigService.getSystemConfigByConfigGroupAndConfigKey(
                SystemConfigComponent.GROUP_AI_MODEL, SystemConfigComponent.KEY_IMAGE_PROVIDER);
        Date now = new Date();
        if (existing == null) {
            SystemConfig config = new SystemConfig();
            config.setConfigGroup(SystemConfigComponent.GROUP_AI_MODEL);
            config.setConfigKey(SystemConfigComponent.KEY_IMAGE_PROVIDER);
            config.setConfigValue(value);
            config.setConfigType(SystemConfigComponent.TYPE_STRING);
            config.setDescription("文生图供应商：dashscope / ark / gpt-image-2（保存即生效）");
            config.setStatus(1);
            config.setCreateTime(now);
            config.setUpdateTime(now);
            systemConfigService.add(config);
        } else {
            SystemConfig update = new SystemConfig();
            update.setConfigValue(value);
            update.setUpdateTime(now);
            systemConfigService.updateSystemConfigByConfigId(update, existing.getConfigId());
        }
        log.info("文生图供应商已切换 provider={}", value);
    }

    // ==================== 提示词三层生效 ====================

    /**
     * 各场景在该学段下的生效情况（Redis 覆盖 → 数据库 → 枚举默认），一次查表避免逐场景查库
     */
    public List<PromptEffectiveVO> promptEffective(String stage) {
        String targetStage = StringTools.isEmpty(stage) ? "ALL" : stage.trim();
        Map<String, PromptTemplate> exactMap = loadPromptRows(targetStage);
        Map<String, PromptTemplate> allMap = "ALL".equals(targetStage) ? exactMap : loadPromptRows("ALL");

        List<PromptEffectiveVO> list = new ArrayList<>();
        for (PromptTypeEnum type : PromptTypeEnum.values()) {
            PromptEffectiveVO vo = new PromptEffectiveVO();
            vo.setScene(type.getScene());
            vo.setTemplateName(type.getTemplateName());
            vo.setStage(targetStage);
            PromptTemplate dbRow = exactMap.get(type.getScene());
            if (dbRow == null) {
                dbRow = allMap.get(type.getScene());
            }
            vo.setDbOverride(dbRow != null);
            vo.setId(dbRow == null ? null : dbRow.getId());
            vo.setStatus(dbRow == null ? null : dbRow.getStatus());
            String redisValue = redisComponent.getString(
                    Constants.REDIS_KEY_PROMPT_TEMPLATE + targetStage + ":" + type.getScene());
            if (!StringTools.isEmpty(redisValue)) {
                vo.setSource(SOURCE_REDIS);
                vo.setContent(redisValue);
            } else if (dbRow != null && !StringTools.isEmpty(dbRow.getContent())) {
                vo.setSource(SOURCE_DB);
                vo.setContent(dbRow.getContent());
            } else {
                vo.setSource(SOURCE_ENUM);
                vo.setContent(type.getDefaultPrompt(targetStage));
            }
            list.add(vo);
        }
        return list;
    }

    private Map<String, PromptTemplate> loadPromptRows(String stage) {
        Map<String, PromptTemplate> map = new HashMap<>();
        try {
            PromptTemplateQuery query = new PromptTemplateQuery();
            query.setStage(stage);
            query.setOrderBy("id asc");
            List<PromptTemplate> rows = promptTemplateService.findListByParam(query);
            if (rows != null) {
                for (PromptTemplate row : rows) {
                    map.putIfAbsent(row.getScene(), row);
                }
            }
        } catch (Exception e) {
            log.warn("读取提示词覆盖失败 stage={}", stage, e);
        }
        return map;
    }

    /**
     * 保存提示词覆盖（按 stage+scene upsert，写库即生效），并清除该场景的 Redis 覆盖
     */
    public void savePrompt(String stage, String scene, String content, Integer status) {
        if (StringTools.isEmpty(scene) || PromptTypeEnum.getByScene(scene) == null) {
            throw new BusinessException("提示词场景不合法：" + scene);
        }
        if (StringTools.isEmpty(content)) {
            throw new BusinessException("提示词内容不能为空");
        }
        if (content.length() > MAX_PROMPT_LENGTH) {
            throw new BusinessException("提示词内容过长（上限 " + MAX_PROMPT_LENGTH + " 字符）");
        }
        String targetStage = StringTools.isEmpty(stage) ? "ALL" : stage.trim();
        boolean stageValid = "ALL".equals(targetStage);
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equals(targetStage)) {
                stageValid = true;
                break;
            }
        }
        if (!stageValid) {
            throw new BusinessException("学段不合法：" + targetStage);
        }
        PromptTypeEnum type = PromptTypeEnum.getByScene(scene);
        Date now = new Date();
        PromptTemplate existing = findPromptRow(targetStage, scene);
        if (existing == null) {
            PromptTemplate row = new PromptTemplate();
            row.setStage(targetStage);
            row.setScene(scene);
            row.setTemplateName(type.getTemplateName());
            row.setContent(content);
            row.setStatus(status == null ? 1 : status);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            promptTemplateService.add(row);
        } else {
            PromptTemplate update = new PromptTemplate();
            update.setTemplateName(type.getTemplateName());
            update.setContent(content);
            update.setStatus(status == null ? 1 : status);
            update.setUpdateTime(now);
            promptTemplateService.updatePromptTemplateById(update, existing.getId());
        }
        // 清掉可能存在的 Redis 覆盖，避免旧覆盖压住刚保存的内容
        redisComponent.removeKey(Constants.REDIS_KEY_PROMPT_TEMPLATE + targetStage + ":" + scene);
        log.info("提示词已保存 stage={} scene={} status={}", targetStage, scene, status);
    }

    private PromptTemplate findPromptRow(String stage, String scene) {
        try {
            PromptTemplateQuery query = new PromptTemplateQuery();
            query.setStage(stage);
            query.setScene(scene);
            query.setOrderBy("id asc");
            List<PromptTemplate> rows = promptTemplateService.findListByParam(query);
            return rows == null || rows.isEmpty() ? null : rows.get(0);
        } catch (Exception e) {
            log.warn("查询提示词覆盖失败 stage={} scene={}", stage, scene, e);
            return null;
        }
    }
}
