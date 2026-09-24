package com.nexora.admin.vo;

import java.util.List;

/**
 * 文生图供应商选项（管理端「环境配置」切换用：当前生效值 + 可选项列表）
 */
public class ImageProviderOptionsVO {

    /** 当前生效的供应商编码：dashscope / ark / gpt-image-2 */
    private String current;

    /** 可选项列表 */
    private List<Option> options;

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    /**
     * 供应商选项
     */
    public static class Option {

        /** 供应商编码 */
        private String code;

        /** 展示名称 */
        private String name;

        /** 说明（Key 环境变量、适用场景等） */
        private String description;

        public Option() {
        }

        public Option(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}