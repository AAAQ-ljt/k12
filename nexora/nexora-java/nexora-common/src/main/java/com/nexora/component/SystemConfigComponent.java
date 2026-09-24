package com.nexora.component;

import com.nexora.entity.po.SystemConfig;
import com.nexora.service.SystemConfigService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统配置读取组件：按 config_group + config_key 读取 system_config 中的可调参数。
 *
 * 设计取舍：
 * - **缺省回落代码默认值**：表里没有该行时直接用默认值，因此不初始化数据也不改变现有行为；
 * - **不缓存**：按唯一键 uk_group_key 单行查询，量小且始终最新，避免缓存失效带来的不一致；
 * - **可调项白名单**：只有 {@link #RAG_DEFINITIONS} 中登记的参数会被业务读取（本期内为 RAG 4 个常用参数），
 *   其余配置仅作展示，不参与运行时。
 */
@Slf4j
@Component
public class SystemConfigComponent {

    /** 配置分组：RAG 检索与入库参数 */
    public static final String GROUP_RAG = "RAG";

    /** 配置分组：AI 模型参数（文生图供应商切换） */
    public static final String GROUP_AI_MODEL = "AI_MODEL";

    /** 文生图供应商配置键（值白名单：dashscope / ark / gpt-image-2） */
    public static final String KEY_IMAGE_PROVIDER = "image_provider";

    /** 文生图供应商编码 */
    public static final String PROVIDER_DASHSCOPE = "dashscope";
    public static final String PROVIDER_ARK = "ark";
    public static final String PROVIDER_GPT_IMAGE_2 = "gpt-image-2";

    /** 配置类型 */
    public static final String TYPE_INT = "INT";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_STRING = "STRING";

    public static final String KEY_RAG_TOP_K = "rag_top_k";
    public static final int DEFAULT_RAG_TOP_K = 10;

    public static final String KEY_RAG_SIMILARITY_THRESHOLD = "rag_similarity_threshold";
    public static final double DEFAULT_RAG_SIMILARITY_THRESHOLD = 0.5;

    public static final String KEY_RAG_CHUNK_SIZE = "rag_chunk_size";
    public static final int DEFAULT_RAG_CHUNK_SIZE = 500;

    public static final String KEY_RAG_EMBEDDING_BATCH_SIZE = "rag_embedding_batch_size";
    public static final int DEFAULT_RAG_EMBEDDING_BATCH_SIZE = 15;

    /** 参数定义（管理端「RAG 配置」页据此展示、校验与恢复默认） */
    public record ConfigDefinition(String configGroup, String configKey, String configName, String configType,
                                   String configValue, Integer minValue, Integer maxValue, String description) {
    }

    public static final List<ConfigDefinition> RAG_DEFINITIONS = List.of(
            new ConfigDefinition(GROUP_RAG, KEY_RAG_TOP_K, "检索召回条数 topK", TYPE_INT,
                    String.valueOf(DEFAULT_RAG_TOP_K), 1, 50,
                    "AI 助教问答时从知识库召回的片段数量，越大越全但更慢"),
            new ConfigDefinition(GROUP_RAG, KEY_RAG_SIMILARITY_THRESHOLD, "相似度阈值", TYPE_FLOAT,
                    String.valueOf(DEFAULT_RAG_SIMILARITY_THRESHOLD), null, null,
                    "低于该相似度的召回结果会被丢弃（0-1，越大越严格）"),
            new ConfigDefinition(GROUP_RAG, KEY_RAG_CHUNK_SIZE, "文档分块大小", TYPE_INT,
                    String.valueOf(DEFAULT_RAG_CHUNK_SIZE), 200, 2000,
                    "文档入库与关键词检索时每块字符数，影响检索粒度"),
            new ConfigDefinition(GROUP_RAG, KEY_RAG_EMBEDDING_BATCH_SIZE, "向量化单批条数", TYPE_INT,
                    String.valueOf(DEFAULT_RAG_EMBEDDING_BATCH_SIZE), 1, 50,
                    "向量化写入 ES 时每批提交的分块数，越大越快但占用更高"));

    @Resource
    private SystemConfigService systemConfigService;

    /**
     * 读取配置项，缺失或读取失败时回落默认值
     */
    public String getValue(String configGroup, String configKey, String defaultValue) {
        if (StringTools.isEmpty(configGroup) || StringTools.isEmpty(configKey)) {
            return defaultValue;
        }
        try {
            SystemConfig config = systemConfigService.getSystemConfigByConfigGroupAndConfigKey(configGroup, configKey);
            if (config == null || StringTools.isEmpty(config.getConfigValue())) {
                return defaultValue;
            }
            return config.getConfigValue().trim();
        } catch (Exception e) {
            log.warn("读取系统配置失败，使用默认值 group={} key={}", configGroup, configKey, e);
            return defaultValue;
        }
    }

    /**
     * 读取整型配置项（非法值回落默认值）
     */
    public int getIntValue(String configGroup, String configKey, int defaultValue) {
        String value = getValue(configGroup, configKey, null);
        if (StringTools.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("系统配置不是合法整数，使用默认值 group={} key={} value={}", configGroup, configKey, value);
            return defaultValue;
        }
    }

    /**
     * 读取浮点配置项（非法值回落默认值）
     */
    public double getDoubleValue(String configGroup, String configKey, double defaultValue) {
        String value = getValue(configGroup, configKey, null);
        if (StringTools.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("系统配置不是合法小数，使用默认值 group={} key={} value={}", configGroup, configKey, value);
            return defaultValue;
        }
    }

    /**
     * 按登记的定义补齐缺失行（幂等，已存在不覆盖），返回新增条数
     */
    public int syncDefaults() {
        int added = 0;
        for (ConfigDefinition definition : RAG_DEFINITIONS) {
            try {
                SystemConfig existing = systemConfigService.getSystemConfigByConfigGroupAndConfigKey(
                        definition.configGroup(), definition.configKey());
                if (existing != null) {
                    continue;
                }
                SystemConfig config = new SystemConfig();
                config.setConfigGroup(definition.configGroup());
                config.setConfigKey(definition.configKey());
                config.setConfigValue(definition.configValue());
                config.setConfigType(definition.configType());
                config.setDescription(definition.description());
                config.setStatus(1);
                config.setCreateTime(new java.util.Date());
                config.setUpdateTime(new java.util.Date());
                systemConfigService.add(config);
                added++;
            } catch (Exception e) {
                log.warn("补齐默认配置失败 group={} key={}", definition.configGroup(), definition.configKey(), e);
            }
        }
        return added;
    }

    /**
     * 查找参数定义（未登记返回 null）
     */
    public ConfigDefinition findDefinition(String configKey) {
        if (StringTools.isEmpty(configKey)) {
            return null;
        }
        for (ConfigDefinition definition : RAG_DEFINITIONS) {
            if (definition.configKey().equals(configKey.trim())) {
                return definition;
            }
        }
        return null;
    }

    /**
     * 参数定义列表（防御性拷贝）
     */
    public List<ConfigDefinition> listDefinitions() {
        return new ArrayList<>(RAG_DEFINITIONS);
    }

    // ==================== 文生图供应商切换 ====================

    /**
     * 读取文生图供应商：优先 system_config 表覆盖值，其次回落启动配置默认值。
     */
    public String getImageProviderValue(String defaultProvider) {
        return getValue(GROUP_AI_MODEL, KEY_IMAGE_PROVIDER, defaultProvider);
    }

    /**
     * 供应商编码规范化：白名单内直接返回；非法 / 空值回落 dashscope。
     */
    public static String normalizeImageProvider(String provider) {
        if (PROVIDER_DASHSCOPE.equals(provider) || PROVIDER_ARK.equals(provider) || PROVIDER_GPT_IMAGE_2.equals(provider)) {
            return provider;
        }
        return PROVIDER_DASHSCOPE;
    }

    /**
     * 是否为受支持的文生图供应商编码
     */
    public static boolean isSupportedImageProvider(String provider) {
        if (provider == null) {
            return false;
        }
        String value = provider.trim();
        return PROVIDER_DASHSCOPE.equals(value) || PROVIDER_ARK.equals(value) || PROVIDER_GPT_IMAGE_2.equals(value);
    }
}
