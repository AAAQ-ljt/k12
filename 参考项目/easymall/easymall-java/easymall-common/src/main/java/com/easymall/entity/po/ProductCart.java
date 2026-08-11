package com.easymall.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 购物车
 */
public class ProductCart implements Serializable {


	/**
	 * 购物车ID
	 */
	private String cartId;

	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 商品ID
	 */
	private String productId;

	/**
	 * 属性值id组
	 */
	private String propertyValueIds;

	/**
	 * 属性值id组hash
	 */
	private String propertyValueIdHash;

	/**
	 * 数量
	 */
	private Integer buyCount;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastUpdateTime;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;


	public void setCartId(String cartId){
		this.cartId = cartId;
	}

	public String getCartId(){
		return this.cartId;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setProductId(String productId){
		this.productId = productId;
	}

	public String getProductId(){
		return this.productId;
	}

	public void setPropertyValueIds(String propertyValueIds){
		this.propertyValueIds = propertyValueIds;
	}

	public String getPropertyValueIds(){
		return this.propertyValueIds;
	}

	public void setPropertyValueIdHash(String propertyValueIdHash){
		this.propertyValueIdHash = propertyValueIdHash;
	}

	public String getPropertyValueIdHash(){
		return this.propertyValueIdHash;
	}

	public void setBuyCount(Integer buyCount){
		this.buyCount = buyCount;
	}

	public Integer getBuyCount(){
		return this.buyCount;
	}

	public void setLastUpdateTime(Date lastUpdateTime){
		this.lastUpdateTime = lastUpdateTime;
	}

	public Date getLastUpdateTime(){
		return this.lastUpdateTime;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	@Override
	public String toString (){
		return "购物车ID:"+(cartId == null ? "空" : cartId)+"，用户ID:"+(userId == null ? "空" : userId)+"，商品ID:"+(productId == null ? "空" : productId)+"，属性值id组:"+(propertyValueIds == null ? "空" : propertyValueIds)+"，属性值id组hash:"+(propertyValueIdHash == null ? "空" : propertyValueIdHash)+"，数量:"+(buyCount == null ? "空" : buyCount)+"，更新时间:"+(lastUpdateTime == null ? "空" : DateUtil.format(lastUpdateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
