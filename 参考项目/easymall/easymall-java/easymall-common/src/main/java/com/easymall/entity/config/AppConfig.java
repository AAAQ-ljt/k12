package com.easymall.entity.config;

import com.easymall.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /**
     * websocket 端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;
    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:admin}")
    private String adminAccount;

    @Value("${admin.password:admin123456}")
    private String adminPassword;


    @Value("${project.domain:}")
    private String projectDomain;

    @Value("${admin.emails:}")
    private String adminEmails;

    //支付宝应用私钥
    @Value("${alipay.appPrivateKey:}")
    private String alipayAppPrivateKey;

    @Value("${alipay.appid:}")
    private String alipayAppid;

    @Value("${alipay.appCertPath:}")
    private String alipayAppCertPath;

    @Value("${alipay.alipayPublicCertPath:}")
    private String alipayPublicCertPath;

    @Value("${alipay.alipayRootCertPath:}")
    private String alipayRootCertPath;


    @Value("${alipay.serverUrl:}")
    private String alipayServerUrl;

    //订单超时
    @Value("${order.expire.minute:5}")
    private Integer orderExpireMinute;


    //自动确认收货
    @Value("${order.confirm.minute:15}")
    private Integer orderConfirmMinute;


    //自动校验订单
    @Value("${project.auto-checkpay:false}")
    private Boolean autoCheckpay;


    //限制ai聊天轮数
    @Value("${project.ai-chat-limit:0}")
    private Integer aiChatLimit;


    public String getProjectFolder() {
        if (!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }

    public Integer getWsPort() {
        return wsPort;
    }

    public Integer getOrderExpireMinute() {
        return orderExpireMinute;
    }

    public String getProjectDomain() {
        return projectDomain;
    }


    public String getAlipayAppCertPath() {
        return alipayAppCertPath;
    }

    public String getAlipayPublicCertPath() {
        return alipayPublicCertPath;
    }

    public String getAlipayRootCertPath() {
        return alipayRootCertPath;
    }

    public Boolean getAutoCheckpay() {
        return autoCheckpay;
    }

    public String getAlipayAppPrivateKey() {
        return alipayAppPrivateKey;
    }

    public String getAlipayAppid() {
        return alipayAppid;
    }

    public String getAlipayServerUrl() {
        return alipayServerUrl;
    }

    public Integer getOrderConfirmMinute() {
        return orderConfirmMinute;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public Integer getAiChatLimit() {
        return aiChatLimit;
    }
}
