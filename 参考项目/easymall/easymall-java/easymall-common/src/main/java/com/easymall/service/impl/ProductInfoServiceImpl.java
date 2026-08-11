package com.easymall.service.impl;

import com.easymall.component.EsSearchComponent;
import com.easymall.component.RedisComponent;
import com.easymall.constants.Constants;
import com.easymall.entity.dto.ProductSaveDTO;
import com.easymall.entity.dto.RagDataDTO;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.enums.ProductStatusEnum;
import com.easymall.entity.enums.RagDataTypeEnum;
import com.easymall.entity.enums.ResponseCodeEnum;
import com.easymall.entity.po.*;
import com.easymall.entity.query.*;
import com.easymall.entity.vo.*;
import com.easymall.exception.BusinessException;
import com.easymall.mappers.ProductInfoMapper;
import com.easymall.mappers.ProductPropertyValueMapper;
import com.easymall.mappers.ProductSkuMapper;
import com.easymall.mappers.SysProductPropertyMapper;
import com.easymall.service.ProductInfoService;
import com.easymall.service.SysCategoryService;
import com.easymall.utils.CollectionComparator;
import com.easymall.utils.CopyTools;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 业务接口实现
 */
@Service("productInfoService")
public class ProductInfoServiceImpl implements ProductInfoService {

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Resource
    private SysProductPropertyMapper<SysProductProperty, SysProductPropertyQuery> sysProductPropertyMapper;

    @Resource
    private ProductPropertyValueMapper<ProductPropertyValue, ProductPropertyValueQuery> productPropertyValueMapper;

    @Resource
    private ProductSkuMapper<ProductSku, ProductSkuQuery> productSkuMapper;

    @Resource
    private SysCategoryService sysCategoryService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private EsSearchComponent esSearchComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ProductInfo> findListByParam(ProductInfoQuery param) {
        return this.productInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ProductInfoQuery param) {
        return this.productInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ProductInfo> findListByPage(ProductInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ProductInfo> list = this.findListByParam(param);
        PaginationResultVO<ProductInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ProductInfo bean) {
        return this.productInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ProductInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.productInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ProductInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.productInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ProductInfo bean, ProductInfoQuery param) {
        StringTools.checkParam(param);
        return this.productInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ProductInfoQuery param) {
        StringTools.checkParam(param);
        return this.productInfoMapper.deleteByParam(param);
    }

    /**
     * 根据ProductId获取对象
     */
    @Override
    public ProductInfo getProductInfoByProductId(String productId) {
        return this.productInfoMapper.selectByProductId(productId);
    }

    /**
     * 根据ProductId修改
     */
    @Override
    public Integer updateProductInfoByProductId(ProductInfo bean, String productId) {
        return this.productInfoMapper.updateByProductId(bean, productId);
    }

    /**
     * 根据ProductId删除
     */
    @Override
    public Integer deleteProductInfoByProductId(String productId) {
        return this.productInfoMapper.deleteByProductId(productId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductSaveDTO productSaveDTO) {
        ProductInfo productInfo = productSaveDTO.getProductInfo();
        List<ProductPropertyValue> productPropertyValueList = productSaveDTO.getProductPropertyList();
        List<ProductSku> skuList = productSaveDTO.getSkuList();

        Boolean isAdd = StringTools.isEmpty(productInfo.getProductId());
        if (isAdd) {
            productInfo.setProductId(StringTools.getRandomNumber(Constants.LENGTH_15));
        }
        productPropertyValueList.forEach(p -> p.setProductId(productInfo.getProductId()));
        skuList.forEach(p -> p.setProductId(productInfo.getProductId()));
        //不接受页面传入的状态
        productInfo.setStatus(null);
        productInfo.setCommendType(null);
        Optional<ProductSku> minPrice = skuList.stream().min(Comparator.comparing(ProductSku::getPrice));
        Optional<ProductSku> maxPrice = skuList.stream().max(Comparator.comparing(ProductSku::getPrice));

        productInfo.setMinPrice(minPrice.get().getPrice());
        productInfo.setMaxPrice(maxPrice.get().getPrice());
        //新增
        if (isAdd) {
            productInfo.setCreateTime(new Date());
            productInfo.setStatus(ProductStatusEnum.OFF_SALE.getStatus());
            productInfoMapper.insert(productInfo);
            productPropertyValueMapper.insertBatch(productPropertyValueList);
            productSkuMapper.insertBatch(skuList);
        } else {
            //获取propertyValue中，新增，修改，删除的
            ProductPropertyValueQuery productPropertyValueQuery = new ProductPropertyValueQuery();
            productPropertyValueQuery.setProductId(productInfo.getProductId());
            List<ProductPropertyValue> dbProductPropertyValueList = productPropertyValueMapper.selectList(productPropertyValueQuery);
           /*
            Map<String, ProductPropertyValue> dbPropertyValueMap = dbProductPropertyValueList.stream().collect(Collectors.toMap(item -> item.getPropertyValueId(),
                    Function.identity(), (data1, data2) -> data2));

            List<ProductPropertyValue> productPropertyValueAddList = new ArrayList<>();
            List<ProductPropertyValue> productPropertyValueUpdateList = new ArrayList<>();
            List<ProductPropertyValue> productPropertyValueDeleteList = new ArrayList<>();
           //参数里有，数据库里没有的 为新增，两个都有为修改
            for (ProductPropertyValue item : productPropertyValueList) {
                if (dbPropertyValueMap.get(item.getPropertyValueId()) == null) {
                    productPropertyValueAddList.add(item);
                } else {
                    productPropertyValueUpdateList.add(item);
                }
            }
            //查询删除，参数里没有，数据库里有的
            Map<String, ProductPropertyValue> propertyValueMap = productPropertyValueList.stream().collect(Collectors.toMap(item -> item.getPropertyValueId(),
                    Function.identity(), (data1, data2) -> data2));
            for (ProductPropertyValue item : dbProductPropertyValueList) {
                if (propertyValueMap.get(item.getPropertyValueId()) == null) {
                    productPropertyValueDeleteList.add(item);
                }
            }

            //获取sku中，新增，修改，删除的
            ProductSkuQuery productSkuQuery = new ProductSkuQuery();
            productSkuQuery.setProductId(productInfo.getProductId());
            List<ProductSku> dbProductSkuList = productSkuMapper.selectList(productSkuQuery);
            Map<String, ProductSku> dbSkuMap = dbProductSkuList.stream().collect(Collectors.toMap(item -> item.getPropertyValueIdGroup(),
                    Function.identity(), (data1, data2) -> data2));

            List<ProductSku> productSkuAddList = new ArrayList<>();
            List<ProductSku> productSkuUpdateList = new ArrayList<>();
            List<ProductSku> productSkuDeleteList = new ArrayList<>();
            //参数里有，数据库里没有的 为新增，两个都有为修改
            for (ProductSku item : skuList) {
                if (dbSkuMap.get(item.getPropertyValueIdGroup()) == null) {
                    productSkuAddList.add(item);
                } else {
                    productSkuUpdateList.add(item);
                }
            }
            //查询删除，参数里没有，数据库里有的
            Map<String, ProductSku> skuMap = skuList.stream().collect(Collectors.toMap(item -> item.getPropertyValueIdGroup(), Function.identity(),
                    (data1, data2) -> data2));
            for (ProductSku item : dbProductSkuList) {
                if (skuMap.get(item.getPropertyValueIdGroup()) == null) {
                    productSkuDeleteList.add(item);
                }
            }*/

            //修改不能修改分类
            productInfo.setCategoryId(null);
            productInfo.setpCategoryId(null);
            productInfo.setStatus(null);
            productInfoMapper.updateByProductId(productInfo, productInfo.getProductId());
            CollectionComparator.DiffResult<ProductPropertyValue> propertyValueResult =
                    new CollectionComparator<ProductPropertyValue>().compare(productPropertyValueList, dbProductPropertyValueList,
                            ProductPropertyValue::getPropertyValueId);
            //属性
            if (!propertyValueResult.addList.isEmpty()) {
                productPropertyValueMapper.insertBatch(propertyValueResult.addList);
            }
            if (!propertyValueResult.deletedList.isEmpty()) {
                productPropertyValueMapper.deleteBatch(productInfo.getProductId(), propertyValueResult.deletedList);
            }
            if (!propertyValueResult.updateList.isEmpty()) {
                productPropertyValueMapper.updateBatch(productInfo.getProductId(), propertyValueResult.updateList);
            }

            //SKU
            ProductSkuQuery productSkuQuery = new ProductSkuQuery();
            productSkuQuery.setProductId(productInfo.getProductId());
            List<ProductSku> dbProductSkuList = productSkuMapper.selectList(productSkuQuery);
            CollectionComparator.DiffResult<ProductSku> skuResult = new CollectionComparator<ProductSku>().compare(skuList, dbProductSkuList,
                    ProductSku::getPropertyValueIdHash);
            if (!skuResult.addList.isEmpty()) {
                productSkuMapper.insertBatch(skuResult.addList);
            }
            if (!skuResult.updateList.isEmpty()) {
                productSkuMapper.updateBatch(productInfo.getProductId(), skuResult.updateList);
            }
            if (!skuResult.deletedList.isEmpty()) {
                productSkuMapper.deleteBatch(productInfo.getProductId(), skuResult.deletedList);
            }
            saveProductInfoExtend(productInfo.getProductId());

        }
    }

    @Override
    public ProductInfoDetailVO getProductInfo(String productId) {
        ProductInfo productInfo = productInfoMapper.selectByProductId(productId);
        if (productInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ProductPropertyValueQuery productPropertyValueQuery = new ProductPropertyValueQuery();
        productPropertyValueQuery.setProductId(productId);
        productPropertyValueQuery.setOrderBy("property_sort asc");
        List<ProductPropertyValue> productPropertyValueList = this.productPropertyValueMapper.selectList(productPropertyValueQuery);

        List<ProductPropertyVO> propertyVOList = new ArrayList<>();
        Map<String, ProductPropertyVO> tempMap = new HashMap<>();
        for (ProductPropertyValue productPropertyValue : productPropertyValueList) {
            ProductPropertyVO productPropertyVO = tempMap.get(productPropertyValue.getPropertyId());
            ProductPropertyValueVO propertyValueVO = new ProductPropertyValueVO(productPropertyValue.getPropertyValueId(), productPropertyValue.getPropertyCover(),
                    productPropertyValue.getPropertyValue(), productPropertyValue.getPropertyRemark());
            if (null == productPropertyVO) {
                productPropertyVO = new ProductPropertyVO(productPropertyValue.getPropertyId(), productPropertyValue.getPropertyName(),
                        productPropertyValue.getPropertySort(), productPropertyValue.getCoverType());
                tempMap.put(productPropertyValue.getPropertyId(), productPropertyVO);
                List<ProductPropertyValueVO> propertyValues = new ArrayList<>();
                propertyValues.add(propertyValueVO);
                productPropertyVO.setPropertyValues(propertyValues);
                propertyVOList.add(productPropertyVO);
            } else {
                productPropertyVO.getPropertyValues().add(propertyValueVO);
            }
        }
        ProductSkuQuery productSkuQuery = new ProductSkuQuery();
        productSkuQuery.setProductId(productId);
        productSkuQuery.setOrderBy("sort asc");
        List<ProductSku> skuList = this.productSkuMapper.selectList(productSkuQuery);
        ProductInfoDetailVO productInfoDetailVO = new ProductInfoDetailVO();
        productInfoDetailVO.setProductInfo(productInfo);
        productInfoDetailVO.setProductPropertyList(propertyVOList);
        productInfoDetailVO.setSkuList(skuList);
        return productInfoDetailVO;
    }

    @Override
    public PaginationResultVO<ProductListVO> findListByPage4ListVO(ProductInfoQuery param) {
        PaginationResultVO<ProductInfo> productInfoVO = findListByPage(param);
        if (productInfoVO.getTotalCount() == 0) {
            return new PaginationResultVO<>(new ArrayList<>());
        }
        List<ProductInfo> productInfoList = productInfoVO.getList();
        //获取分类映射
        SysCategoryQuery categoryQuery = new SysCategoryQuery();
        categoryQuery.setConvert2Tree(false);
        List<SysCategory> categoryList = sysCategoryService.findListByParam(categoryQuery);
        Map<String, SysCategory> categoryMap = categoryList.stream().collect(Collectors.toMap(item -> item.getCategoryId(), item -> item));

        //查询sku
        List<String> productIdList = productInfoList.stream().map(item -> item.getProductId()).collect(Collectors.toList());

        ProductSkuQuery productSkuQuery = new ProductSkuQuery();
        productSkuQuery.setProductIdList(productIdList);
        List<ProductSku> allSkuList = productSkuMapper.selectList(productSkuQuery);

        Map<String, List<ProductSku>> skuMap = allSkuList.stream().collect(Collectors.groupingBy(ProductSku::getProductId));

        List<ProductListVO> productListVOList = productInfoList.stream().map(item -> {
            ProductListVO productListVO = CopyTools.copy(item, ProductListVO.class);
            //分类
            productListVO.setCategoryName(categoryMap.get(item.getpCategoryId()).getCategoryName() + "/" + categoryMap.get(item.getCategoryId()).getCategoryName());
            List<ProductSku> skuList = skuMap.get(item.getProductId());
            //sku数量
            productListVO.setSkuCount(skuList.size());
            productListVO.setTotalStock(skuList.stream().mapToInt(sku -> sku.getStock()).sum());
            return productListVO;
        }).collect(Collectors.toList());
        return new PaginationResultVO<>(productInfoVO.getTotalCount(), productInfoVO.getPageSize(), productInfoVO.getPageNo(), productInfoVO.getPageTotal(),
                productListVOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String productId, Integer status) {
        ProductStatusEnum statusEnum = ProductStatusEnum.getByStatus(status);
        if (null == statusEnum || statusEnum == ProductStatusEnum.DELETE) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ProductInfo productInfo = new ProductInfo();
        productInfo.setStatus(status);
        this.updateProductInfoByProductId(productInfo, productId);

        saveProductInfoExtend(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(String productId) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setStatus(ProductStatusEnum.DELETE.getStatus());
        this.productInfoMapper.updateByProductId(productInfo, productId);

        saveProductInfoExtend(productId);
    }

    private void saveProductInfoExtend(String productId) {
        esSearchComponent.saveProduct(productId);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisComponent.sendMessage(Constants.REDIS_QUEUE_RAG_DATA, new RagDataDTO(productId, RagDataTypeEnum.PRODUCT.getType()));
                    }
                }
        );
    }
}