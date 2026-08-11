package com.easymall.mappers;

import com.easymall.entity.po.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作接口
 */
public interface ProductSkuMapper<T, P> extends BaseMapper<T, P> {


    /**
     * 根据ProductIdAndPropertyValueIdHash更新
     */
    Integer updateByProductIdAndPropertyValueIdHash(@Param("bean") T t, @Param("productId") String productId, @Param("propertyValueIdHash") String propertyValueIdHash);


    /**
     * 根据ProductIdAndPropertyValueIdHash删除
     */
    Integer deleteByProductIdAndPropertyValueIdHash(@Param("productId") String productId, @Param("propertyValueIdHash") String propertyValueIdHash);


    /**
     * 根据ProductIdAndPropertyValueIdHash获取对象
     */
    T selectByProductIdAndPropertyValueIdHash(@Param("productId") String productId, @Param("propertyValueIdHash") String propertyValueIdHash);


    void deleteBatch(@Param("productId") String productId, @Param("dataList") List<T> dataList);

    void updateBatch(@Param("productId") String productId, @Param("dataList") List<T> dataList);

    Integer updateStock(@Param("productId") String productId, @Param("propertyValueIdHash") String propertyValueIdHash, @Param("changeStock") Integer changeStock);

    Integer updateStockBatch(@Param("orderItemList") List<OrderItem> orderItemList);
}
