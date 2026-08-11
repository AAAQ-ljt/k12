package com.easymall.controller.notify;

import com.easymall.controller.ABaseController;
import com.easymall.entity.enums.PayChannelEnum;
import com.easymall.exception.BusinessException;
import com.easymall.service.OrderInfoService;
import com.easymall.utils.JsonUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/notify")
@Validated
@Slf4j
public class AlipayNotifyController extends ABaseController {


    @Resource
    private OrderInfoService orderInfoService;

    @ResponseBody
    @RequestMapping("/alipayNotify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            Map<String, String> requestParams = convertRequestParamsToMap(request);
            log.info("支付宝回调请求参数params：{}", JsonUtils.convertObj2Json(requestParams));
            orderInfoService.payNotify(PayChannelEnum.ALIPAY_PC, requestParams, null);
            return STATUC_SUCCESS;
        } catch (BusinessException e) {
            log.error("支付宝回调处理失败", e);
            return STATUC_ERROR;
        } catch (Exception e) {
            log.error("支付宝回调处理失败", e);
            return STATUC_ERROR;
        }
    }

    private Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String> retMap = new HashMap();

        Set<Map.Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

        for (Map.Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            int valLen = values.length;
            if (valLen == 1) {
                retMap.put(name, values[0]);
            } else if (valLen > 1) {
                StringBuilder sb = new StringBuilder();
                for (String val : values) {
                    sb.append(",").append(val);
                }
                retMap.put(name, sb.toString().substring(1));
            } else {
                retMap.put(name, "");
            }
        }

        return retMap;
    }
}
