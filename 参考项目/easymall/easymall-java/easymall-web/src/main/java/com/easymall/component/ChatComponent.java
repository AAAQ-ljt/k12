package com.easymall.component;

import com.easymall.constants.Constants;
import com.easymall.entity.config.AppConfig;
import com.easymall.entity.dto.MessageSendDTO;
import com.easymall.entity.enums.*;
import com.easymall.entity.po.AgentMessage;
import com.easymall.entity.po.OrderInfo;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.query.AgentMessageQuery;
import com.easymall.entity.query.OrderInfoQuery;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.exception.BusinessException;
import com.easymall.service.AgentMessageService;
import com.easymall.service.OrderInfoService;
import com.easymall.service.ProductInfoService;
import com.easymall.utils.DateUtil;
import com.easymall.utils.JsonUtils;
import com.easymall.utils.StringTools;
import com.easymall.websocket.message.MessageHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChatComponent {

    @Resource
    private ChatClient chatClient;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    private VectorStore vectorStore;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private AgentMessageService agentMessageService;

    @Resource
    private AppConfig appConfig;

    //用户取消
    public void cancelMessage(String userId, Integer messageId) {
        agentMessageService.cancelMessage(userId, messageId);
        redisComponent.cancelMessage(userId, messageId);
    }

    public AgentMessage sendMessage(String userId, String userMessage) {

        if (appConfig.getAiChatLimit() != 0) {
            AgentMessageQuery agentMessageQuery = new AgentMessageQuery();
            agentMessageQuery.setUserId(userId);
            Integer chatCount = agentMessageService.findCountByParam(agentMessageQuery);
            if (chatCount >= appConfig.getAiChatLimit()) {
                throw new BusinessException("AI购物体验已经结束");
            }
        }

        AgentMessage agentMessage = agentMessageService.saveMessage(userId, userMessage);
        //异步调用模型回复消息
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            assistantAnswer(agentMessage);
        });
        return agentMessage;
    }

    public void assistantAnswer(AgentMessage agentMessage) {
        UserIntent userIntent = analyzeUserIntent(agentMessage.getUserId(), agentMessage.getUserMessage());
        PromptTypeEnum promptTypeEnum = PromptTypeEnum.getByCode(userIntent.intent);
        promptTypeEnum = promptTypeEnum == null ? PromptTypeEnum.CHAT : promptTypeEnum;

        //用户意图分析完成后，提前判断一下是否取消
        if (redisComponent.hasCancelMessage(agentMessage.getUserId(), agentMessage.getMessageId())) {
            return;
        }
        switch (promptTypeEnum) {
            case PRODUCT_SEARCH://搜索商品
                searchProduct(agentMessage, userIntent.data);
                agentMessageService.completeMessage(agentMessage.getMessageId(), agentMessage.getBizType(), agentMessage.getAssistantMessage(),
                        agentMessage.getBizData());
                break;
            case QUERY_ORDER://查询订单
                queryOrder(agentMessage, userIntent.data);
                agentMessageService.completeMessage(agentMessage.getMessageId(), agentMessage.getBizType(), agentMessage.getAssistantMessage(),
                        agentMessage.getBizData());
                break;
            default:
                chat(promptTypeEnum, agentMessage);
        }
    }

    record ProductIdInfo(String productId) {

    }

    private String buildTypeFilter(String dataType) {
        return String.format("dataType == '%s'", dataType);
    }

    //搜索商品
    private void searchProduct(AgentMessage agentMessage, String keyword) {
        keyword = StringTools.isEmpty(keyword) ? agentMessage.getUserMessage() : keyword;
        SearchRequest request =
                SearchRequest.builder().query(keyword).filterExpression(buildTypeFilter(RagDataTypeEnum.PRODUCT.getType())).similarityThreshold(0.25).topK(30).build();
        List<Document> docs = vectorStore.similaritySearch(request);
        String prompt = getPrompt(PromptTypeEnum.PRODUCT_SEARCH, JsonUtils.convertObj2Json(docs), keyword, null);
        List<ProductIdInfo> productIdInfos = getChatClientRequestSpec().user(prompt).call().entity(new ParameterizedTypeReference<>() {
        });
        List<String> productIdList = productIdInfos.stream().map(ProductIdInfo::productId).toList();
        List<ProductInfo> productInfoList = new ArrayList<>();
        if (!productIdInfos.isEmpty()) {
            ProductInfoQuery productInfoQuery = new ProductInfoQuery();
            productInfoQuery.setOrderBy("create_time desc");
            productInfoQuery.setProductIdList(productIdList);
            productInfoList = productInfoService.findListByParam(productInfoQuery);
        }
        //记录数据库
        String assistantData = JsonUtils.convertObj2Json(productInfoList);
        agentMessage.setBizType(UserIntentEnum.PRODUCT_SEARCH.getKey());
        agentMessage.setAssistantMessage(assistantData);
        agentMessage.setBizData(productIdList.isEmpty() ? null : JsonUtils.convertObj2Json(productIdList));

        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setUserId(agentMessage.getUserId());
        messageSendDTO.setOutPutType(MessageOutPutTypeEnum.DONE.getType());
        messageSendDTO.setAssistantMessage(assistantData);
        messageSendDTO.setBizType(UserIntentEnum.PRODUCT_SEARCH.getKey());
        messageSendDTO.setMessageId(agentMessage.getMessageId());
        messageHandler.sendMessage(messageSendDTO);
    }

    private void queryOrder(AgentMessage agentMessage, String orderId) {
        OrderInfoQuery orderInfoQuery = new OrderInfoQuery();
        orderInfoQuery.setOrderTimeStart(DateUtil.getBeforeDay(Constants.LATEST_ORDER_DAYS, DateTimePatternEnum.YYYY_MM_DD.getPattern()));
        orderInfoQuery.setOrderTimeEnd(DateUtil.format(new Date(), DateTimePatternEnum.YYYY_MM_DD.getPattern()));
        orderInfoQuery.setOrderId(orderId);
        orderInfoQuery.setUserId(agentMessage.getUserId());
        orderInfoQuery.setOrderBy("o.order_time desc");
        orderInfoQuery.setQueryItems(true);
        orderInfoQuery.setExecuteOrderStatusList(new Integer[]{OrderStatusEnum.DELETE.getStatus()});
        List<OrderInfo> orderInfoList = orderInfoService.findListByParam(orderInfoQuery);

        String assistantData = JsonUtils.convertObj2Json(orderInfoList);
        agentMessage.setBizType(UserIntentEnum.QUERY_ORDER.getKey());
        agentMessage.setAssistantMessage(assistantData);
        //订单ID存入业务数据
        List<String> orderIdList = orderInfoList.stream().map(OrderInfo::getOrderId).toList();
        agentMessage.setBizData(orderIdList.isEmpty() ? null : JsonUtils.convertObj2Json(orderIdList));
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setUserId(agentMessage.getUserId());
        messageSendDTO.setOutPutType(MessageOutPutTypeEnum.DONE.getType());
        messageSendDTO.setAssistantMessage(assistantData);
        messageSendDTO.setBizType(UserIntentEnum.QUERY_ORDER.getKey());
        messageSendDTO.setMessageId(agentMessage.getMessageId());
        messageHandler.sendMessage(messageSendDTO);
    }

    private List<Message> getHistoryMessage(String userId, String currentMessage) {
        AgentMessageQuery agentMessageQuery = new AgentMessageQuery();
        agentMessageQuery.setUserId(userId);
        agentMessageQuery.setOrderBy("message_id desc");
        agentMessageQuery.setSimplePage(new SimplePage(1, Constants.LENGTH_10));
        List<AgentMessage> agentMessageList = agentMessageService.findListByParam(agentMessageQuery);
        agentMessageList.sort(Comparator.comparingInt(AgentMessage::getMessageId));
        List<Message> historyList = new ArrayList<>();
        for (AgentMessage agentMessage : agentMessageList) {
            if (ArrayUtils.contains(new String[]{
                    UserIntentEnum.PRODUCT_SEARCH.getKey(),
                    UserIntentEnum.QUERY_ORDER.getKey()}, agentMessage.getBizType()) && agentMessage.getBizData() != null) {
                historyList.add(new UserMessage(agentMessage.getUserMessage()));
                historyList.add(new AssistantMessage(agentMessage.getBizData()));
                continue;
            }
            if (!StringTools.isEmpty(agentMessage.getAssistantMessage())) {
                historyList.add(new UserMessage(agentMessage.getUserMessage()));
                historyList.add(new AssistantMessage(agentMessage.getAssistantMessage()));
            }
        }
        historyList.add(new UserMessage(currentMessage));
        return historyList;
    }

    //大模型组织语言回答
    private void chat(PromptTypeEnum promptTypeEnum, AgentMessage agentMessage) {
        List<String> chatMessage = new ArrayList<>();
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setUserId(agentMessage.getUserId());
        try {
            String ragData = "";
            if (promptTypeEnum == PromptTypeEnum.CHAT) {
                SearchRequest request =
                        SearchRequest.builder().query(agentMessage.getUserMessage()).filterExpression(buildTypeFilter(RagDataTypeEnum.FAQ.getType())).similarityThreshold(0.5).topK(15).build();
                List<Document> docs = vectorStore.similaritySearch(request);
                docs = docs.stream().distinct().collect(Collectors.toList());
                ragData = docs.stream().map(doc -> doc.getText()).collect(Collectors.joining("\n\n"));
            }
            String prompt = getPrompt(promptTypeEnum, agentMessage.getUserId(), agentMessage.getUserMessage(), ragData);
            getChatClientRequestSpec().toolCallbacks(toolCallbackProvider).messages(getHistoryMessage(agentMessage.getUserId(), prompt)).stream().chatResponse().doOnNext(response -> {
                String responseMessage = response.getResults().get(0).getOutput().getText();
                if (!StringTools.isEmpty(responseMessage)) {
                    messageSendDTO.setOutPutType(MessageOutPutTypeEnum.OUTPUTTING.getType());
                    messageSendDTO.setAssistantMessage(responseMessage);
                    messageSendDTO.setMessageId(agentMessage.getMessageId());
                    messageHandler.sendMessage(messageSendDTO);
                    chatMessage.add(responseMessage);
                }
            }).doOnComplete(() -> {
                messageSendDTO.setOutPutType(MessageOutPutTypeEnum.DONE.getType());
                messageSendDTO.setMessageId(agentMessage.getMessageId());
                messageHandler.sendMessage(messageSendDTO);

                agentMessage.setAssistantMessage(chatMessage.stream().collect(Collectors.joining("")));
                agentMessageService.completeMessage(agentMessage.getMessageId(), UserIntentEnum.CHAT.getKey(), agentMessage.getAssistantMessage(), null);


            }).doOnError(error -> {
                messageSendDTO.setOutPutType(MessageOutPutTypeEnum.ERROR.getType());
                messageSendDTO.setAssistantMessage(ResponseCodeEnum.CODE_500.getMsg());
                messageSendDTO.setMessageId(agentMessage.getMessageId());
                messageHandler.sendMessage(messageSendDTO);

                agentMessage.setAssistantMessage(chatMessage.stream().collect(Collectors.joining("")));
                agentMessageService.completeMessage(agentMessage.getMessageId(), UserIntentEnum.CHAT.getKey(), agentMessage.getAssistantMessage(), null);

            }).subscribe();
        } catch (Exception e) {
            log.error("调用大模型失败", e);
            messageSendDTO.setOutPutType(MessageOutPutTypeEnum.ERROR.getType());
            messageSendDTO.setMessageId(agentMessage.getMessageId());
            messageSendDTO.setAssistantMessage(ResponseCodeEnum.CODE_500.getMsg());
            messageHandler.sendMessage(messageSendDTO);

            agentMessage.setAssistantMessage(ResponseCodeEnum.CODE_500.getMsg());
            agentMessageService.completeMessage(agentMessage.getMessageId(), UserIntentEnum.CHAT.getKey(), agentMessage.getAssistantMessage(), null);
        }
    }

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

    private ChatClient.ChatClientRequestSpec getChatClientRequestSpec() {
        String systemPrompt = getPrompt(PromptTypeEnum.GLOBAL, null, null, null);
        return chatClient.prompt().system(systemPrompt).advisors(new LoggerAdvisor());
    }

    private UserIntent analyzeUserIntent(String userId, String userMessage) {
        String prompt = getPrompt(PromptTypeEnum.USER_INTENT, userId, userMessage, null);
        UserIntent userIntent = getChatClientRequestSpec().messages(getHistoryMessage(userId, prompt)).call().entity(UserIntent.class);
        return userIntent;
    }

    record UserIntent(String intent, String data) {

    }
}
