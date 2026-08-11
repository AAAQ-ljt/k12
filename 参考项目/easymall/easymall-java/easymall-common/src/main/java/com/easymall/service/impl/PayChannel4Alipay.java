package com.easymall.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.easymall.entity.config.AppConfig;
import com.easymall.entity.dto.PayInfoDTO;
import com.easymall.entity.dto.PayOrderNotifyDTO;
import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.entity.enums.PayChannelEnum;
import com.easymall.exception.BusinessException;
import com.easymall.service.PayChannel;
import com.easymall.utils.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service("payChannel4Alipay")
public class PayChannel4Alipay implements PayChannel {

    static {
        // 设置SSL协议版本
        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
    }

    private static final String TRADE_STATE_SUCCESS = "TRADE_SUCCESS";

    /**
     * 交易不存在
     */
    private static final String TRADE_NOT_EXIST = "ACQ.TRADE_NOT_EXIST";

    private static final String NOTIFY_URL = "/api/notify/alipayNotify";

    @Resource
    private AppConfig appConfig;

    @Override
    public PayInfoDTO getPayUrl(PayChannelEnum payChannelEnum, String payOrderId, String subject, BigDecimal amount) {
        try {
            // 初始化SDK
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            // 构造请求参数以调用接口
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            // 设置商户订单号
            model.setOutTradeNo(payOrderId);
            // 设置订单总金额
            model.setTotalAmount(amount.toString());
            // 设置订单标题
            model.setSubject(subject);
            //订单过期时间
            model.setTimeExpire(DateUtil.getMinAfter(appConfig.getOrderExpireMinute(), DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));

            request.setBizModel(model);

            //实际开发会设置异步通知回调信息，以便实时获取订单支付结果
            request.setNotifyUrl(appConfig.getProjectDomain() + NOTIFY_URL);
            String payInfo = null;
            switch (payChannelEnum) {
                case ALIPAY_PC:
                    model.setProductCode("FAST_INSTANT_TRADE_PAY");
                    AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
                    if (!response.isSuccess()) {
                        throw new BusinessException("获取支付信息失败");
                    }
                    payInfo = response.getBody();
                    break;
            }
            return new PayInfoDTO(payInfo, payOrderId, amount);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝支付获取支付信息失败", e);
            throw new BusinessException("获取支付信息失败");
        }
    }

    private AlipayConfig getAlipayConfig() {
        AlipayConfig config = new AlipayConfig();
        //支付宝网关
        config.setServerUrl(appConfig.getAlipayServerUrl());
        //应用ID
        config.setAppId(appConfig.getAlipayAppid());
        //应用私钥信息
        config.setPrivateKey(appConfig.getAlipayAppPrivateKey());
        //应用公钥证书本地地址
        config.setAppCertPath(appConfig.getProjectFolder() + appConfig.getAlipayAppCertPath());
        //支付宝公钥证书 alipayPublicCert.crt
        config.setAlipayPublicCertPath(appConfig.getProjectFolder() + appConfig.getAlipayPublicCertPath());
        //支付宝根证书本地地址
        config.setRootCertPath(appConfig.getProjectFolder() + appConfig.getAlipayRootCertPath());
        config.setCharset("UTF8");
        config.setSignType("RSA2");
        return config;
    }

    @Override
    public PayOrderNotifyDTO payNotify(Map<String, String> requestParams, String jsonBody) {
        try {
            requestParams.remove("sign_type");
            Boolean signCheckResult = AlipaySignature.rsaCertCheckV2(requestParams, appConfig.getAlipayAppCertPath(), "UTF-8", "RSA2");
            if (!signCheckResult) {
                throw new BusinessException("支付宝回调校验失败");
            }
        } catch (AlipayApiException e) {
            log.error("支付宝回调检验失败", e);
            throw new BusinessException("支付宝回调校验失败");
        }
        String payOrderId = requestParams.get("out_trade_no");
        String channelOrderId = requestParams.get("trade_no");
        String status = String.valueOf(requestParams.get("trade_status"));
        if (!TRADE_STATE_SUCCESS.equalsIgnoreCase(status)) {
            log.info("支付宝回调地址状态不为success，不做处理，订单号：{}", payOrderId);
            return null;
        }
        return new PayOrderNotifyDTO(payOrderId, channelOrderId);
    }

    @Override
    public PayOrderNotifyDTO queryOrder(String payOrderId) {
        try {
            // 初始化SDK
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            // 设置商户订单号
            model.setOutTradeNo(payOrderId);
            request.setBizModel(model);
            AlipayTradeQueryResponse response = alipayClient.certificateExecute(request);

            //订单未支付
            if (!response.isSuccess() || !TRADE_STATE_SUCCESS.equals(response.getTradeStatus())) {
                return null;
            }
            log.info("查询支付宝订单:{},返回结果:{}", payOrderId, response.getBody());
            return new PayOrderNotifyDTO(payOrderId, response.getTradeNo());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝获取支付信息失败", e);
            throw new BusinessException("获取支付信息失败");
        }
    }

    @Override
    public void refund(String sourcePayOrderId, String payOrderId, BigDecimal refundAmount) {
        try {
            // 初始化SDK
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            // 设置商户订单号
            model.setOutTradeNo(sourcePayOrderId);
            model.setOutRequestNo(payOrderId);
            model.setRefundAmount(refundAmount.toString());
            request.setBizModel(model);
            AlipayTradeRefundResponse response = alipayClient.certificateExecute(request);
            log.info("支付宝退款:{},返回结果:{}", payOrderId, response.getBody());
            if (!response.isSuccess()) {
                throw new BusinessException("退款失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝退款失败", e);
            throw new BusinessException("支付宝退款失败");
        }
    }

    @Override
    public void closeOrder(String payOrderId) {
        try {
            AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            // 设置商户订单号
            model.setOutTradeNo(payOrderId);
            request.setBizModel(model);
            AlipayTradeCloseResponse response = alipayClient.certificateExecute(request);
            if (!response.isSuccess() && !TRADE_NOT_EXIST.equals(response.getSubCode())) {
                throw new BusinessException("订单关闭失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("支付宝订单关闭失败", e);
            throw new BusinessException("订单关闭失败");
        }
    }
}
