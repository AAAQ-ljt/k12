package com.easymall.entity.enums;

import com.easymall.utils.StringTools;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum PromptTypeEnum {

    GLOBAL("global", """
            # 角色与核心目标
            你是智能购物助手AI，负责为用户提供全流程电商服务。你的核心目标是：精准理解用户需求、提供个性化购物建议、高效处理订单与售后问题，并保持友好专业的服务态度。
            """, "全局系统提示词"),

    USER_INTENT("userIntent", """
            请分析用户消息的购物意图,并提取关键信息。
            
            用户ID：%s
            用户问题: %s
            
            请从以下意图类型中选择最匹配的一个：
            INTENT TYPES:
            - PRODUCT_SEARCH: 搜索商品、想买东西、查看商品信息（如：我想买手机，找一下连衣裙），关键信息返回具体的商品信息。
            - QUERY_ORDER: 订单查询（如：我的订单？）,如用户提供订单号（例如：20251229161636SGUCPYYI1TXOEPHY），将订单号解析出来用data返回,如果无法解析订单好，不要再data中返回
            - REFUND: 退款退货申请（如：我想退款，商品有问题要退货）
            - CANCEL_ORDER: 取消已下单但未发货或未完成的订单（如：我不想要了，帮我取消订单，取消我刚买的那个东西）
            - CONFIRM_RECEIPT: 用户确认已收到货物（如：我已经收到货了，点哪里确认收货，那个东西我拿到了）
            - QUERY_LOGISTICS: 物流查询、快递跟踪、包裹状态查询（如：我的快递到哪了？查一下物流，跟踪包裹）
            - PRODUCT_REVIEW: 用户主动对已收货商品给出评价（如："给上个订单好评"、"这东西太差了，我要评价"、"给一个好评吧"、"给一个差评"）
            - CHAT: 一般性问题，比如 问候、打招呼，商品对比，购物细则等
            
            返回JSON格式，包含以下字段：
            - intentType: 意图类型（使用上面的类型值）
            - data: 关键信息，比如订单号（如果可以提取到订单号就提取，如果无法提取，就不返回，不要随便捏造订单号，订单号类似这样:20251229161636SGUCPYYI1TXOEPHY）
            示例响应：
            {
              "intentType": "PRODUCT_SEARCH",
              "data":"20251229161636SGUCPYYI1TXOEPHY"
            }
            """, "用户意图提示词"),
    PRODUCT_SEARCH("product_search", """
            请基于以下商品信息回答用户问题：
            商品数据：
            ```json
              %s
            ```
            用户问题：
            %s
            
            请严格按照以下要求执行：
            1. 只返回JSON数组，不要有任何额外文本
            2. JSON数组中每个对象只包含productId字段
            3. 只使用提供的商品信息中的ID，不要自己编造ID
            4. 如果用户问题不明确或没有相关商品，返回空数组：[]
            5. 严格按照以下格式：
            [
              {"productId": "147099495496439"},
              {"productId": "326818100160840"}
            ]
            """, "商品搜索"),
    QUERY_ORDER("query_order", "", "查询订单"),
    REFUND("refund", """
             处理退款相关问题。
             用户ID：%s
             用户问题:%s
             **第一步：识别订单号**
             - 用户取消订单，如果无法解析用户的订单号，请用户提供订单信息,不要捏造订单号，正确的订单号是时间戳加随机字母（例如：20251229161636SGUCPYYI1TXOEPHY）
             **第二步：调用工具进行退款**
             - 根据工具返回结果，友好告知用户结果。
            """, "退款"),
    CANCEL_ORDER("cancel_order", """
             用户希望取消一个处于“待付款”、可取消状态的订单。典型表述包括：“我要取消订单”、“不买了帮我取消”、“取消我刚下的单”、“订单号XXXX我不要了”。
             用户ID：%s
             用户问题:%s
             **第一步：识别订单号**
             - 用户取消订单，如果无法解析用户的订单号，请用户提供订单信息,不要捏造订单号，正确的订单号是时间戳加随机字母（例如：20251229161636SGUCPYYI1TXOEPHY）
             **第二步：调用工具进行订单取消**
             - 根据工具返回结果，友好告知用户结果。
            """, "取消订单"),
    CONFIRM_RECEIPT("confirm_receipt", """
             用户主动表示已收到货物，并可能想手动完成“确认收货”操作以结束交易流程。典型表述包括：“货已收到，怎么确认收货？”、“东西拿到了，点哪里确认？”、“我已经收到包裹了”
             用户ID：%s
             用户问题:%s
             **第一步：识别订单号**
             - 用户确认收货，如果无法解析用户的订单号，请用户提供订单信息,不要捏造订单号，正确的订单号是时间戳加随机字母（例如：20251229161636SGUCPYYI1TXOEPHY）
             **第二步：调用工具进行确认收货**
             - 根据工具返回结果，友好告知用户结果。
            """, "确认收货"),
    QUERY_LOGISTICS("query_logistics", """
            查询物流信息。
            用户ID：%s
            用户问题：%s
             **第一步：识别订单号**
             - 用户查询订单物流，如果无法解析用户的订单号，请用户提供订单信息,不要捏造订单号，正确的订单号是时间戳加随机字母（例如：20251229161636SGUCPYYI1TXOEPHY）
             **第二步：调用工具查询物流信息**
             - 根据工具返回结果，友好告知用户详细的物流信息。
            """, "查询物流信息"),
    PRODUCT_REVIEW("product_review", """
            处理订单评价。
            用户ID：%s
            用户信息：%s
            
            三步处理：
            
            1. **信息提取**
               - 订单ID：从问题中提取或引导提供
               - 评价内容：用户对商品的描述
            
            2. **自动评星（基于情感分析）**
               ⭐ 1星：非常负面（垃圾、糟糕、差评）
               ⭐⭐ 2星：负面（失望、不好、后悔）
               ⭐⭐⭐ 3星：中性（还行、一般、马马虎虎）
               ⭐⭐⭐⭐ 4星：正面（不错、挺好、满意）
               ⭐⭐⭐⭐⭐ 5星：非常正面（完美、超棒、强烈推荐）
            """, "评价订单"),
    CHAT("chat", """
             请基于以下知识库内容回答用户问题，保持友好专业：
             回答要求：
             1. 只基于提供的知识库内容回答，不要编造信息
             2. 回答要专业、友好、简洁
             3. 对于政策类问题，需要明确说明条件和限制
             4. 如果涉及多个方面，请分点说明
             5. 知识库中包含图片信息也一并返回不要过滤
             知识库内容：
             %s
            
             用户ID：%s
            
             用户问题:%s
            
            """, "一般性问题"),
    ;

    private String key;
    private String prompt;
    private String desc;

    PromptTypeEnum(String key, String prompt, String desc) {
        this.key = key;
        this.prompt = prompt;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getDesc() {
        return desc;
    }

    public static PromptTypeEnum getByCode(String code) {
        Optional<PromptTypeEnum> typeEnum = Arrays.stream(PromptTypeEnum.values()).filter(value -> value.toString().equals(code)).findFirst();
        return typeEnum == null || typeEnum.isEmpty() ? null : typeEnum.get();
    }

    public static PromptTypeEnum getByKey(String key) {
        Optional<PromptTypeEnum> typeEnum = Arrays.stream(PromptTypeEnum.values()).filter(value -> value.getKey().equals(key)).findFirst();
        return typeEnum == null || typeEnum.isEmpty() ? null : typeEnum.get();
    }


    public record Prompt(String key, String prompt, String desc) {
        public static Prompt of(PromptTypeEnum typeEnum) {
            return new Prompt(typeEnum.getKey(), typeEnum.getPrompt(), typeEnum.getDesc());
        }
    }

    public static List<Prompt> getPrompts() {
        return Arrays.stream(PromptTypeEnum.values()).filter(value -> !StringTools.isEmpty(value.getPrompt())).map(Prompt::of).toList();
    }
}
