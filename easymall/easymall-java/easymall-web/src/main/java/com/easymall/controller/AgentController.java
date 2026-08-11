package com.easymall.controller;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.component.ChatComponent;
import com.easymall.component.RedisComponent;
import com.easymall.entity.enums.PromptTypeEnum;
import com.easymall.entity.enums.RagDataTypeEnum;
import com.easymall.entity.po.AgentMessage;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.po.ProductPropertyValue;
import com.easymall.entity.po.ProductSku;
import com.easymall.entity.query.AgentMessageQuery;
import com.easymall.entity.query.ProductPropertyValueQuery;
import com.easymall.entity.query.ProductSkuQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.AgentMessageService;
import com.easymall.service.ProductInfoService;
import com.easymall.service.ProductPropertyValueService;
import com.easymall.service.ProductSkuService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent")
@Slf4j
public class AgentController extends ABaseController {

    @Resource
    private ChatComponent chatComponent;

    @Resource
    private AgentMessageService agentMessageService;

    @RequestMapping("/sendMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO sendMessage(String message) {
        AgentMessage agentMessage = chatComponent.sendMessage(getTokenUserInfo().getUserId(), message);
        return getSuccessResponseVO(agentMessage);
    }


    @RequestMapping("/cancelMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO cancelMessage(Integer messageId) {
        chatComponent.cancelMessage(getTokenUserInfo().getUserId(), messageId);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/loadHistoryMessage")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadHistoryMessage(Integer pageNo, Integer maxMessageId) {
        AgentMessageQuery agentMessageQuery = new AgentMessageQuery();
        agentMessageQuery.setOrderBy("message_id desc");
        agentMessageQuery.setPageNo(pageNo);
        agentMessageQuery.setUserId(getTokenUserInfo().getUserId());
        agentMessageQuery.setMaxMessageId(maxMessageId);
        PaginationResultVO resultVO = agentMessageService.findListByPage(agentMessageQuery);
        return getSuccessResponseVO(resultVO);
    }

    @Resource
    private VectorStore vectorStore;

    @RequestMapping("/test")
    public ResponseVO test() {
        SearchRequest request =
                SearchRequest.builder().query("我要买一双男鞋").topK(5).build();
        List<Document> docs = vectorStore.similaritySearch(request);
        return getSuccessResponseVO(docs);
    }

    private String buildTypeFilter(String dataType) {
        return String.format("dataType == '%s'", dataType);
    }

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductPropertyValueService productPropertyValueService;

    @Resource
    private ProductSkuService productSkuService;

    @RequestMapping("/testadd")
    public ResponseVO testadd() {

        ProductInfo productInfo = this.productInfoService.getProductInfoByProductId("153133309154815");

        ProductPropertyValueQuery productPropertyValueQuery = new ProductPropertyValueQuery();
        productPropertyValueQuery.setProductId(productInfo.getProductId());
        productPropertyValueQuery.setOrderBy("property_sort asc");
        List<ProductPropertyValue> productPropertyValueList = this.productPropertyValueService.findListByParam(productPropertyValueQuery);

        Map<String, ProductPropertyValue> productPropertyValueMap = productPropertyValueList.stream().collect(Collectors.toMap(item -> item.getPropertyValueId(),
                Function.identity(), (data1, data2) -> data2));

        ProductSkuQuery productSkuQuery = new ProductSkuQuery();
        productSkuQuery.setProductId(productInfo.getProductId());
        List<ProductSku> allSkuList = productSkuService.findListByParam(productSkuQuery);

        List<Document> list = new ArrayList<>();


        //不带sku信息
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("dataType", RagDataTypeEnum.PRODUCT.getType());
        metaData.put("productId", productInfo.getProductId());
        metaData.put("productName", productInfo.getProductName());
        StringBuilder content = new StringBuilder();
        content.append(productInfo.getProductName()).append("。");
        Document doc = new Document(productInfo.getProductId(), productInfo.getProductName(), metaData);
        list.add(doc);

        List<Map<String, Object>> skuList = new ArrayList<>();

        for (ProductSku sku : allSkuList) {
            content = new StringBuilder();
            metaData = new HashMap<>();
            metaData.put("dataType", RagDataTypeEnum.PRODUCT.getType());
            metaData.put("productId", productInfo.getProductId());
            metaData.put("productName", productInfo.getProductName());
            content.append("商品名称：").append(productInfo.getProductName()).append("。");
            Map<String, Object> skuProperty = new HashMap<>();
            String[] propertyValueIdArray = sku.getPropertyValueIds().split("-");
            StringBuffer skuContent = new StringBuffer();
            Integer index = 0;
            for (String propertyValueId : propertyValueIdArray) {
                index++;
                ProductPropertyValue propertyValue = productPropertyValueMap.get(propertyValueId);
                // content.append(propertyValue.getPropertyValue()).append(" ");
                skuContent = skuContent.append(propertyValue.getPropertyName()).append("：").append(propertyValue.getPropertyValue());
                if (index < propertyValueIdArray.length) {
                    skuContent.append("，");
                }
            }
            content.append(skuContent);
            skuProperty.put("property", skuContent);
            skuList.add(skuProperty);
            metaData.put("skuList", skuList);
            doc = new Document(productInfo.getProductId() + sku.getPropertyValueIdHash(), content.toString(), metaData);
            list.add(doc);
        }
        // metaData = new HashMap<>();
        // metaData.put("dataType", RagDataTypeEnum.PRODUCT.getType());

        vectorStore.add(list);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/testrag")
    public ResponseVO testrag() {

        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder().build());
        return getSuccessResponseVO(documents);
    }

    @Resource
    private ChatClient chatClient;

    @RequestMapping("/testMessage")
    public ResponseVO testMessage() {
        String systemPrompt = getPrompt(PromptTypeEnum.GLOBAL, null, null, null);
        String prompt = """
                    请分析用户消息的购物意图,并提取关键信息。
                    用户问题: %s
                
                    请从以下意图类型中选择最匹配的一个：
                    INTENT TYPES:
                    - PRODUCT_SEARCH: 搜索商品、想买东西、查看商品信息（如：我想买手机，找一下连衣裙）
                    - QUERY_ORDER: 订单查询（如：我的订单？）,如用户提供订单号（例如：20251229161636SGUCPYYI1TXOEPHY），将订单号解析出来用data返回,如果无法解析订单好，不要再data中返回
                    - REFUND: 退款退货申请（如：我想退款，商品有问题要退货）
                    - CANCEL_ORDER: 取消已下单但未发货或未完成的订单（如：我不想要了，帮我取消订单，取消我刚买的那个东西）
                    - CONFIRM_RECEIPT: 用户确认已收到货物（如：我已经收到货了，点哪里确认收货，那个东西我拿到了）
                    - QUERY_LOGISTICS: 物流查询、快递跟踪、包裹状态查询（如：我的快递到哪了？查一下物流，跟踪包裹）
                    - PRODUCT_REVIEW: 用户主动对已收货商品给出评价（如："给上个订单好评"、"这东西太差了，我要评价"、"给一个好评吧"、"给一个差评"）
                    - CHAT: 一般性问题，比如 问候、打招呼，商品对比，购物细则等
                      返回JSON格式，包含以下字段：
                    - intentType: 意图类型（使用上面的类型值）
                    - orderId: 订单号（如果可以提取就提取，如果无法提取，就不返回，不要随便捏造订单号，订单号类似这样:20251229161636SGUCPYYI1TXOEPHY）
                    示例响应：
                    {
                      "intentType": "PRODUCT_SEARCH",
                      "orderId":"20251229161636SGUCPYYI1TXOEPHY"
                    }
                """;
        prompt = String.format(prompt, "我想买一副老罗真迹 白鸟朝凤图");

        UserIntent userIntent = chatClient.prompt().system(systemPrompt).user(prompt).call().entity(UserIntent.class);

        return getSuccessResponseVO(userIntent);
    }

    record UserIntent(String intent, String data) {

    }

    @Resource
    private RedisComponent redisComponent;

    private String getPrompt(PromptTypeEnum promptType, String userId, String message, String ragData) {
        String prompt = redisComponent.getPrompt(promptType.getKey());
        prompt = prompt == null ? promptType.getPrompt() : prompt;
        if (PromptTypeEnum.CHAT == promptType) {
            ragData = StringTools.isEmpty(ragData) ? "知识库中暂无暂无相关内容" : ragData;
            prompt = String.format(prompt, ragData, userId, message);
            return prompt;
        }
        if (PromptTypeEnum.GLOBAL != promptType) {
            prompt = String.format(prompt, userId, message);
            return prompt;
        }
        return prompt;
    }
}
