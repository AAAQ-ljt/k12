package com.easymall.entity.query;

import java.math.BigDecimal;
import java.util.List;


/**
 * 商品信息参数
 */
public class ProductInfoQuery extends BaseParam {


    /**
     * 商品ID
     */
    private String productId;

    private String productIdFuzzy;

    /**
     * 商品名称
     */
    private String productName;

    private String productNameFuzzy;

    /**
     * 商品描述
     */
    private String productDesc;

    private String productDescFuzzy;

    /**
     * 封面
     */
    private String cover;

    private String coverFuzzy;

    /**
     * 创建时间
     */
    private String createTime;

    private String createTimeStart;

    private String createTimeEnd;

    /**
     * 分类ID
     */
    private String categoryId;

    private String categoryIdFuzzy;

    /**
     * 分类父ID
     */
    private String pCategoryId;

    private String pCategoryIdFuzzy;

    /**
     * -1:已删除 0:下架  1:上架
     */
    private Integer status;

    /**
     * 最低价格
     */
    private BigDecimal minPrice;

    /**
     * 最高价格
     */
    private BigDecimal maxPrice;

    /**
     * 销量
     */
    private Integer totalSale;

    /**
     * 0:未推荐 1:已经推荐
     */
    private Integer commendType;


    private String categoryIdOrPCategoryId;

    private List<String> productIdList;

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductIdFuzzy(String productIdFuzzy) {
        this.productIdFuzzy = productIdFuzzy;
    }

    public String getProductIdFuzzy() {
        return this.productIdFuzzy;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductNameFuzzy(String productNameFuzzy) {
        this.productNameFuzzy = productNameFuzzy;
    }

    public String getProductNameFuzzy() {
        return this.productNameFuzzy;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getProductDesc() {
        return this.productDesc;
    }

    public void setProductDescFuzzy(String productDescFuzzy) {
        this.productDescFuzzy = productDescFuzzy;
    }

    public String getProductDescFuzzy() {
        return this.productDescFuzzy;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCoverFuzzy(String coverFuzzy) {
        this.coverFuzzy = coverFuzzy;
    }

    public String getCoverFuzzy() {
        return this.coverFuzzy;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeStart() {
        return this.createTimeStart;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getCreateTimeEnd() {
        return this.createTimeEnd;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryIdFuzzy(String categoryIdFuzzy) {
        this.categoryIdFuzzy = categoryIdFuzzy;
    }

    public String getCategoryIdFuzzy() {
        return this.categoryIdFuzzy;
    }

    public void setpCategoryId(String pCategoryId) {
        this.pCategoryId = pCategoryId;
    }

    public String getpCategoryId() {
        return this.pCategoryId;
    }

    public void setpCategoryIdFuzzy(String pCategoryIdFuzzy) {
        this.pCategoryIdFuzzy = pCategoryIdFuzzy;
    }

    public String getpCategoryIdFuzzy() {
        return this.pCategoryIdFuzzy;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMinPrice() {
        return this.minPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public BigDecimal getMaxPrice() {
        return this.maxPrice;
    }

    public void setTotalSale(Integer totalSale) {
        this.totalSale = totalSale;
    }

    public Integer getTotalSale() {
        return this.totalSale;
    }

    public void setCommendType(Integer commendType) {
        this.commendType = commendType;
    }

    public Integer getCommendType() {
        return this.commendType;
    }

    public String getCategoryIdOrPCategoryId() {
        return categoryIdOrPCategoryId;
    }

    public void setCategoryIdOrPCategoryId(String categoryIdOrPCategoryId) {
        this.categoryIdOrPCategoryId = categoryIdOrPCategoryId;
    }

    public List<String> getProductIdList() {
        return productIdList;
    }

    public void setProductIdList(List<String> productIdList) {
        this.productIdList = productIdList;
    }
}
