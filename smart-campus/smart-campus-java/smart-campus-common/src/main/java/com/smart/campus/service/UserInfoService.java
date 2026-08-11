package com.smart.campus.service;

import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;

import java.util.List;

public interface UserInfoService {

    List<UserInfo> findListByParam(UserInfoQuery param);

    Integer findCountByParam(UserInfoQuery param);

    PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param);

    Integer add(UserInfo bean);

    Integer addBatch(List<UserInfo> listBean);

    Integer addOrUpdateBatch(List<UserInfo> listBean);

    Integer updateByParam(UserInfo bean, UserInfoQuery param);

    Integer deleteByParam(UserInfoQuery param);

    UserInfo getUserInfoByUserId(Integer userId);

    Integer updateUserInfoByUserId(UserInfo bean, Integer userId);

    Integer deleteUserInfoByUserId(Integer userId);

    List<UserInfo> getUserInfoByUserIdList(List<Integer> userIdList);

    Integer deleteBatchByUserIdList(List<Integer> userIdList);

    UserInfo getUserInfoByUserNo(String userNo);

    Integer updateUserInfoByUserNo(UserInfo bean, String userNo);

    Integer deleteUserInfoByUserNo(String userNo);

    UserInfo getUserInfoByPhone(String phone);

    Integer updateUserInfoByPhone(UserInfo bean, String phone);

    Integer deleteUserInfoByPhone(String phone);

    List<Integer> getUsedClassIdList(List<Integer> classIdList);
}
