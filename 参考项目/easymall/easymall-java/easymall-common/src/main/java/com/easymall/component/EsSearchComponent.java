package com.easymall.component;

import com.easymall.entity.dto.ProductInfoDTO;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.enums.ProductStatusEnum;
import com.easymall.entity.enums.SearchFieldTypeEnum;
import com.easymall.entity.enums.SearchSortTypeEnum;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.mappers.ProductInfoMapper;
import com.easymall.utils.CopyTools;
import com.easymall.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EsSearchComponent {
    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @PostConstruct
    public void createIndexWithIK() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ProductInfoDTO.class);
            if (indexOps.exists()) {
                return;
            }
            String json = """
                    {
                      "analysis": {
                        "analyzer": {
                          "ik_max_word": {
                            "type": "custom",
                            "tokenizer": "ik_max_word"
                          },
                          "ik_smart": {
                            "type": "custom",
                            "tokenizer": "ik_smart"
                          }
                        }
                      }
                    }
                    """;
            // 创建索引并应用设置
            indexOps.create(JsonUtils.convertJson2Obj(json, Map.class));
            // 创建映射（会应用 @Field 注解）
            Document mapping = indexOps.createMapping(ProductInfoDTO.class);
            indexOps.putMapping(mapping);
            log.info("索引创建成功，已应用IK分词器");
        } catch (Exception e) {
            log.error("创建索引失败", e);
            throw new RuntimeException("创建索引失败", e);
        }
    }

    public PaginationResultVO<ProductInfoDTO> searchProducts(String keyWords, BigDecimal priceFrom, BigDecimal priceTo, String sortType, String sortField,
                                                             Integer pageNo) {
        try {
            // 参数处理
            pageNo = pageNo == null ? 1 : pageNo;
            //es 分页从0开始
            pageNo = pageNo - 1;
            int pageSize = PageSize.SIZE15.getSize();
            // 1. 构建查询条件
            Criteria criteria = new Criteria();


            // 针对短词优化：如果搜索词很短（<=2个字），使用更宽松的匹配
            if (keyWords.length() <= 2) {
                // 使用bool查询，提高召回率
                Criteria nameCriteria = new Criteria();

                // 方式1：普通分词匹配
                nameCriteria = nameCriteria.or(new Criteria("productName").contains(keyWords));

                // 方式2：通配符匹配
                nameCriteria = nameCriteria.or(new Criteria("productName").expression("*" + keyWords + "*"));

                // 方式3：短语匹配（适合短词）
                nameCriteria = nameCriteria.or(new Criteria("productName").matches(keyWords));

                criteria = criteria.and(nameCriteria);
            } else {
                // 长词使用普通分词匹配
                criteria = criteria.and("productName").contains(keyWords);
            }

            // 价格过滤
            if (priceFrom != null || priceTo != null) {
                Criteria priceCriteria = new Criteria();
                priceCriteria = priceCriteria.and("minPrice");
                if (priceFrom != null) {
                    priceCriteria = priceCriteria.greaterThanEqual(priceFrom);
                }
                if (priceTo != null) {
                    priceCriteria = priceCriteria.lessThanEqual(priceTo);
                }
                criteria = criteria.and(priceCriteria);
            }

            SearchSortTypeEnum sortTypeEnum = SearchSortTypeEnum.getByType(sortType);
            sortTypeEnum = sortTypeEnum == null ? SearchSortTypeEnum.DESC : sortTypeEnum;

            SearchFieldTypeEnum fieldTypeEnum = SearchFieldTypeEnum.getByFieldType(sortField);
            fieldTypeEnum = fieldTypeEnum == null ? SearchFieldTypeEnum.COMPOSITE : fieldTypeEnum;

            // 2. 分页和排序
            Sort sort = Sort.by(sortTypeEnum.getDirection(), fieldTypeEnum.getField());
            Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
            // 3. 执行查询
            CriteriaQuery query = new CriteriaQuery(criteria);
            query.setPageable(pageable);
            SearchHits<ProductInfoDTO> searchHits = elasticsearchOperations.search(query, ProductInfoDTO.class);
            // 4. 处理结果
            List<ProductInfoDTO> products = searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList());
            long totalHits = searchHits.getTotalHits();
            int totalPages = (int) Math.ceil((double) totalHits / pageSize);
            return new PaginationResultVO((int) totalHits, pageSize, pageNo + 1, totalPages, products);
        } catch (Exception e) {
            log.error("搜索失败", e);
            return new PaginationResultVO<>(0, PageSize.SIZE15.getSize(), pageNo != null ? pageNo : 0, 0, new ArrayList<>());
        }
    }

    public void saveProduct(String productId) {
        ProductInfo product = productInfoMapper.selectByProductId(productId);
        ProductInfoDTO productInfoDTO = CopyTools.copy(product, ProductInfoDTO.class);
        if (ProductStatusEnum.ON_SALE.getStatus().equals(product.getStatus())) {
            elasticsearchOperations.save(productInfoDTO);
        } else {
            elasticsearchOperations.delete(productId, ProductInfoDTO.class);
        }
    }
}
