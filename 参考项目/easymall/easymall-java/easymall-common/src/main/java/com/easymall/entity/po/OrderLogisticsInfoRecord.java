package com.easymall.entity.po;

import java.util.Date;
import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 
 */
public class OrderLogisticsInfoRecord implements Serializable {


	/**
	 * 记录ID
	 */
	private Integer recordId;

	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 记录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date recordTime;

	/**
	 * 记录地址
	 */
	private String recordAddress;


	public void setRecordId(Integer recordId){
		this.recordId = recordId;
	}

	public Integer getRecordId(){
		return this.recordId;
	}

	public void setOrderId(String orderId){
		this.orderId = orderId;
	}

	public String getOrderId(){
		return this.orderId;
	}

	public void setRecordTime(Date recordTime){
		this.recordTime = recordTime;
	}

	public Date getRecordTime(){
		return this.recordTime;
	}

	public void setRecordAddress(String recordAddress){
		this.recordAddress = recordAddress;
	}

	public String getRecordAddress(){
		return this.recordAddress;
	}

	@Override
	public String toString (){
		return "记录ID:"+(recordId == null ? "空" : recordId)+"，订单ID:"+(orderId == null ? "空" : orderId)+"，记录时间:"+(recordTime == null ? "空" : DateUtil.format(recordTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，记录地址:"+(recordAddress == null ? "空" : recordAddress);
	}
}
