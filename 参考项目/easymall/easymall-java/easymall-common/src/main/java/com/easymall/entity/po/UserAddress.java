package com.easymall.entity.po;

import com.easymall.entity.valid.UpdateGroup;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;


/**
 *
 */
public class UserAddress implements Serializable {


    /**
     * 地址ID
     */
    @NotEmpty(groups = UpdateGroup.class)
    private String addressId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 详细地址
     */
    @NotEmpty
    private String address;

    /**
     * 收货人
     */
    @NotEmpty
    private String addressee;

    /**
     * 手机号码
     */
    @NotEmpty
    private String phone;

    /**
     * 默认类型0:非默认  1:默认
     */
    private Integer defaultType;

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getAddressId() {
        return this.addressId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee;
    }

    public String getAddressee() {
        return this.addressee;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setDefaultType(Integer defaultType) {
        this.defaultType = defaultType;
    }

    public Integer getDefaultType() {
        return this.defaultType;
    }

    @Override
    public String toString() {
        return "地址ID:" + (addressId == null ? "空" : addressId) + "，用户ID:" + (userId == null ? "空" : userId) + "，详细地址:" + (address == null ? "空" : address) + "，收货人:" + (addressee == null ? "空" : addressee) + "，手机号码:" + (phone == null ? "空" : phone) + "，默认类型0:非默认  1:默认:" + (defaultType == null ? "空" : defaultType);
    }
}
