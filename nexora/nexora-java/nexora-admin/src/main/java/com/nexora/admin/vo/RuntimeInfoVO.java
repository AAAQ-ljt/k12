package com.nexora.admin.vo;

import java.util.List;

/**
 * 运行时环境与模型信息（管理端「模型与提示词 / 环境配置」只读展示；Key 一律掩码）
 */
public class RuntimeInfoVO {

    /** 当前激活的 profile */
    private String profile;

    /** 服务端口 */
    private String serverPort;

    /** 基础设施项：MySQL / Redis / ES / 项目目录等 */
    private List<RuntimeItemVO> infrastructure;

    /** 模型项：对话 / 视觉 / 向量 / 文生图（含供应商与模型名，Key 已掩码） */
    private List<RuntimeItemVO> models;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public List<RuntimeItemVO> getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(List<RuntimeItemVO> infrastructure) {
        this.infrastructure = infrastructure;
    }

    public List<RuntimeItemVO> getModels() {
        return models;
    }

    public void setModels(List<RuntimeItemVO> models) {
        this.models = models;
    }
}
