package com.nexora.admin.vo;

/**
 * RAG 可调参数项（管理端「系统设置 → RAG 配置」）
 */
public class RagConfigItemVO {

    /** 配置项 key */
    private String configKey;

    /** 配置项名称 */
    private String configName;

    /** 值类型：INT / FLOAT */
    private String configType;

    /** 说明 */
    private String description;

    /** 当前生效值（未定制时等于默认值） */
    private String currentValue;

    /** 代码默认值（恢复默认用） */
    private String defaultValue;

    /** 允许的最小值，可空 */
    private Integer minValue;

    /** 允许的最大值，可空 */
    private Integer maxValue;

    /** 是否已被管理端定制（true=数据库有覆盖值） */
    private Boolean customized;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public Boolean getCustomized() {
        return customized;
    }

    public void setCustomized(Boolean customized) {
        this.customized = customized;
    }
}
