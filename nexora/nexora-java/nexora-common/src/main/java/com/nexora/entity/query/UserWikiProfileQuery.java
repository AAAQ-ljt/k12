package com.nexora.entity.query;

/**
 * 学生学习档案 查询参数
 */
public class UserWikiProfileQuery extends BaseParam {

    /**
     * 学生ID
     */
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}