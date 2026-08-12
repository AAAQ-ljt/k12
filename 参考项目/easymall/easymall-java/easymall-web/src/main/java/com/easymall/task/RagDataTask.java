package com.easymall.task;

import com.easymall.constants.Constants;
import com.easymall.entity.dto.RagDataDTO;
import com.easymall.entity.enums.ExecutorServiceSingletonEnum;
import com.easymall.entity.enums.ProductStatusEnum;
import com.easymall.entity.enums.RagDataTypeEnum;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.po.ProductPropertyValue;
import com.easymall.entity.po.ProductSku;
import com.easymall.entity.po.RagQuestion;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.query.ProductPropertyValueQuery;
import com.easymall.entity.query.ProductSkuQuery;
import com.easymall.mappers.ProductInfoMapper;
import com.easymall.mappers.ProductPropertyValueMapper;
import com.easymall.mappers.ProductSkuMapper;
import com.easymall.service.RagQuestionService;
import com.easymall.utils.JsonUtils;
import com.easymall.utils.StringTools;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RagDataTask {

    @Resource
    private VectorStore vectorStore;

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Resource
    private ProductPropertyValueMapper<ProductPropertyValue, ProductPropertyValueQuery> productPropertyValueMapper;

    @Resource
    private ProductSkuMapper<ProductSku, ProductSkuQuery> productSkuMapper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RagQuestionService ragQuestionService;

    @PostConstruct
    public void subscribe() {
        ExecutorServiceSingletonEnum.INSTANCE.getExecutorService().execute(() -> {
            RBlockingQueue<RagDataDTO> queue = redissonClient.getBlockingQueue(Constants.REDIS_QUEUE_RAG_DATA, JsonJacksonCodec.INSTANCE);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // 阻塞获取消息
                    RagDataDTO message = queue.take();
                    log.info("消费消息: {}", JsonUtils.convertObj2Json(message));
                    RagDataTypeEnum typeEnum = RagDataTypeEnum.getByType(message.getType());
                    switch (typeEnum) {
                        case PRODUCT:
                            saveData2VectorDB4Product(message.getDataId());
                            break;
                        case FAQ:
                            saveData2VectorDB4FAQ(message.getDataId());
                            break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("消息消费被中断");
                    break;
                } catch (Exception e) {
                    log.error("处理消息失败", e);
                }
            }
        });
    }

    //将商品信息更新到向量数据库
    private void saveData2VectorDB4Product(String productId) {
        ProductInfo productInfo = this.productInfoMapper.selectByProductId(productId);
        if (!ProductStatusEnum.ON_SALE.getStatus().equals(productInfo.getStatus())) {
            vectorStore.delete(RagDataTypeEnum.PRODUCT.getType() + productId);
            return;
        }
        ProductPropertyValueQuery productPropertyValueQuery = new ProductPropertyValueQuery();
        productPropertyValueQuery.setProductId(productId);
        productPropertyValueQuery.setOrderBy("property_sort asc");
        List<ProductPropertyValue> productPropertyValueList = this.productPropertyValueMapper.selectList(productPropertyValueQuery);

        Map<String, ProductPropertyValue> productPropertyValueMap = productPropertyValueList.stream().collect(Collectors.toMap(item -> item.getPropertyValueId(),
                Function.identity(), (data1, data2) -> data2));

        ProductSkuQuery productSkuQuery = new ProductSkuQuery();
        productSkuQuery.setProductId(productId);
        List<ProductSku> allSkuList = productSkuMapper.selectList(productSkuQuery);

        List<Document> list = new ArrayList<>();

        //不带sku信息
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("dataType", RagDataTypeEnum.PRODUCT.getType());
        metaData.put("productId", productInfo.getProductId());
        metaData.put("productName", productInfo.getProductName());
        Document doc = new Document(productInfo.getProductId(), productInfo.getProductName(), metaData);
        list.add(doc);

        List<Map<String, Object>> skuList = new ArrayList<>();
        for (ProductSku sku : allSkuList) {
            metaData = new HashMap<>();
            metaData.put("dataType", RagDataTypeEnum.PRODUCT.getType());
            metaData.put("productId", productInfo.getProductId());
            metaData.put("productName", productInfo.getProductName());
            StringBuilder content = new StringBuilder();
            content.append(productInfo.getProductName()).append(",");
            Map<String, Object> skuProperty = new HashMap<>();
            String[] propertyValueIdArray = sku.getPropertyValueIds().split("-");
            StringBuffer skuContent = new StringBuffer();
            Integer index = 0;
            for (String propertyValueId : propertyValueIdArray) {
                index++;
                ProductPropertyValue propertyValue = productPropertyValueMap.get(propertyValueId);
                skuContent = skuContent.append(propertyValue.getPropertyName()).append("：").append(propertyValue.getPropertyValue());
                if (index < propertyValueIdArray.length) {
                    skuContent.append(",");
                }
            }
            content.append(skuContent);
            skuProperty.put("property", skuContent);
            skuList.add(skuProperty);
            metaData.put("skuList", skuList);
            doc = new Document(productInfo.getProductId() + "_" + sku.getPropertyValueIdHash(), content.toString(), metaData);
            list.add(doc);
            //百炼一次只能处理10个
            if (list.size() >= 10) {
                vectorStore.add(list);
                list.clear();
            }
        }

        if (!list.isEmpty()) {
            vectorStore.add(list);
        }
    }

    private void saveData2VectorDB4FAQ(String questionId) {
        RagQuestion ragQuestion = ragQuestionService.getRagQuestionByQuestionId(Integer.parseInt(questionId));
        if (ragQuestion == null) {
            vectorStore.delete(RagDataTypeEnum.FAQ.getType() + questionId);
            return;
        }
        List<Document> list = new ArrayList<>();
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("dataType", RagDataTypeEnum.FAQ.getType());
        metaData.put("questionId", ragQuestion.getQuestionId());
        metaData.put("question", ragQuestion.getQuestion());
        metaData.put("answer", ragQuestion.getAnswer());
        if (!StringTools.isEmpty(ragQuestion.getSimilarQuestion())) {
            metaData.put("similarQuestion", ragQuestion.getSimilarQuestion());
        } else {
            metaData.put("similarQuestion", "暂无相似问法");
        }

        StringBuilder content = new StringBuilder();
        // 核心问题
        content.append(ragQuestion.getQuestion()).append(" ");
        // 相似问题（如果有）
        if (!StringTools.isEmpty(ragQuestion.getSimilarQuestion())) {
            content.append("相似问题：").append(ragQuestion.getSimilarQuestion()).append(" ");
        }
        // 答案
        content.append("答案是：").append(ragQuestion.getAnswer());
        Document doc = new Document(questionId, content.toString(), metaData);
        list.add(doc);
        vectorStore.add(list);
    }
}