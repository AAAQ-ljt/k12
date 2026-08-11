package com.easymall.service.impl;

import com.easymall.entity.enums.PageSize;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.po.ProductPropertyValue;
import com.easymall.entity.po.ProductSku;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.query.ProductPropertyValueQuery;
import com.easymall.entity.query.ProductSkuQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ProductSkuProperDataVO;
import com.easymall.entity.vo.ProductSkuVO;
import com.easymall.exception.BusinessException;
import com.easymall.mappers.ProductInfoMapper;
import com.easymall.mappers.ProductPropertyValueMapper;
import com.easymall.mappers.ProductSkuMapper;
import com.easymall.service.ProductSkuService;
import com.easymall.utils.CopyTools;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 业务接口实现
 */
@Service("productSkuService")
public class ProductSkuServiceImpl implements ProductSkuService {

    @Resource
    private ProductSkuMapper<ProductSku, ProductSkuQuery> productSkuMapper;

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Resource
    private ProductPropertyValueMapper<ProductPropertyValue, ProductPropertyValueQuery> productPropertyValueMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ProductSku> findListByParam(ProductSkuQuery param) {
        return this.productSkuMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ProductSkuQuery param) {
        return this.productSkuMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ProductSku> findListByPage(ProductSkuQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ProductSku> list = this.findListByParam(param);
        PaginationResultVO<ProductSku> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ProductSku bean) {
        return this.productSkuMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ProductSku> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.productSkuMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ProductSku> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.productSkuMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ProductSku bean, ProductSkuQuery param) {
        StringTools.checkParam(param);
        return this.productSkuMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ProductSkuQuery param) {
        StringTools.checkParam(param);
        return this.productSkuMapper.deleteByParam(param);
    }

    /**
     * 根据ProductIdAndPropertyValueIdHash获取对象
     */
    @Override
    public ProductSku getProductSkuByProductIdAndPropertyValueIdHash(String productId, String propertyValueIdHash) {
        return this.productSkuMapper.selectByProductIdAndPropertyValueIdHash(productId, propertyValueIdHash);
    }

    /**
     * 根据ProductIdAndPropertyValueIdHash修改
     */
    @Override
    public Integer updateProductSkuByProductIdAndPropertyValueIdHash(ProductSku bean, String productId, String propertyValueIdHash) {
        return this.productSkuMapper.updateByProductIdAndPropertyValueIdHash(bean, productId, propertyValueIdHash);
    }

    /**
     * 根据ProductIdAndPropertyValueIdHash删除
     */
    @Override
    public Integer deleteProductSkuByProductIdAndPropertyValueIdHash(String productId, String propertyValueIdHash) {
        return this.productSkuMapper.deleteByProductIdAndPropertyValueIdHash(productId, propertyValueIdHash);
    }

    @Override
    public void updateStock(String productId, String propertyValueIdHash, Integer changeStock) {
        Integer changeCount = this.productSkuMapper.updateStock(productId, propertyValueIdHash, changeStock);
        if (changeCount == 0) {
            throw new BusinessException("库存不足");
        }
    }


    @Override
    public PaginationResultVO<ProductSkuVO> findListByPage4ListVO(ProductSkuQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ProductSku> list = this.findListByParam(param);
        if (list.isEmpty()) {
            return new PaginationResultVO<>(0, param.getPageSize(), param.getPageNo(), 0, new ArrayList<>());
        }
        List<String> productIdList = list.stream().map(ProductSku::getProductId).toList();
        //查询商品信息
        ProductInfoQuery productInfoQuery = new ProductInfoQuery();
        productInfoQuery.setProductIdList(productIdList);
        List<ProductInfo> productInfoList = productInfoMapper.selectList(productInfoQuery);

        Map<String, ProductInfo> tempProductInfoMap = productInfoList.stream().collect(Collectors.toMap(item -> item.getProductId(), Function.identity(), (data1,
                                                                                                                                                           data2) -> data2));
        //查询商品属性信息
        ProductPropertyValueQuery productPropertyValueQuery = new ProductPropertyValueQuery();
        productPropertyValueQuery.setProductIdList(productIdList);
        List<ProductPropertyValue> productPropertyValueList = productPropertyValueMapper.selectList(productPropertyValueQuery);
        Map<String, ProductPropertyValue> productPropertyValueMap =
                productPropertyValueList.stream().collect(Collectors.toMap(item -> item.getProductId() + item.getPropertyValueId(), Function.identity(),
                        (data1, data2) -> data2));

        List<ProductSkuVO> productSkuVOList = new ArrayList<>();
        for (ProductSku sku : list) {
            ProductSkuVO productSkuVO = CopyTools.copy(sku, ProductSkuVO.class);
            String propertyValueIds = sku.getPropertyValueIds();
            String[] propertyValueIdArray = propertyValueIds.split("-");
            List<ProductSkuProperDataVO> propertyData = new ArrayList<>();
            String cover = null;
            for (String propertyValueId : propertyValueIdArray) {
                ProductSkuProperDataVO productSkuProperDataVO = new ProductSkuProperDataVO();
                ProductPropertyValue productPropertyValue = productPropertyValueMap.get(sku.getProductId() + propertyValueId);
                if (productPropertyValue == null) {
                    continue;
                }
                productSkuProperDataVO.setPropertyName(productPropertyValue.getPropertyName());
                productSkuProperDataVO.setPropertyValue(productPropertyValue.getPropertyValue());
                propertyData.add(productSkuProperDataVO);

                if (cover == null && !StringTools.isEmpty(productPropertyValue.getPropertyCover())) {
                    cover = productPropertyValue.getPropertyCover();
                }
            }
            productSkuVO.setPropertyData(propertyData);
            ProductInfo productInfo = tempProductInfoMap.get(sku.getProductId());
            productSkuVO.setProductName(productInfo.getProductName());
            //设置封面
            cover = cover == null ? productInfo.getCover().split(",")[0] : cover;
            productSkuVO.setProductCover(cover);
            productSkuVOList.add(productSkuVO);
        }
        PaginationResultVO<ProductSkuVO> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), productSkuVOList);
        return result;
    }
}