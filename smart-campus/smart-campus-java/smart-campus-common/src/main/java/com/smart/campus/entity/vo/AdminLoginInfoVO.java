package com.smart.campus.entity.vo;

import java.util.List;

public class AdminLoginInfoVO {

    private String token;

    private LoginUserVO userInfo;

    private List<SystemMenuVO> menuList;

    private List<String> menuCodes;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LoginUserVO getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(LoginUserVO userInfo) {
        this.userInfo = userInfo;
    }

    public List<SystemMenuVO> getMenuList() {
        return menuList;
    }

    public void setMenuList(List<SystemMenuVO> menuList) {
        this.menuList = menuList;
    }

    public List<String> getMenuCodes() {
        return menuCodes;
    }

    public void setMenuCodes(List<String> menuCodes) {
        this.menuCodes = menuCodes;
    }
}
