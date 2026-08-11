package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.UserInfoMapper;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    private static final String DEFAULT_PASSWORD = "123456";

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        return new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
        StringTools.checkParam(param);
        prepareUserForSave(bean, false);
        return this.userInfoMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(UserInfoQuery param) {
        StringTools.checkParam(param);
        return this.userInfoMapper.deleteByParam(param);
    }

    @Override
    public UserInfo getUserInfoByUserId(Integer userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, Integer userId) {
        prepareUserForSave(bean, false);
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    @Override
    public Integer deleteUserInfoByUserId(Integer userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    @Override
    public List<UserInfo> getUserInfoByUserIdList(List<Integer> userIdList) {
        if (userIdList == null || userIdList.isEmpty()) {
            return List.of();
        }
        return this.userInfoMapper.selectByUserIdList(userIdList);
    }

    @Override
    public Integer deleteBatchByUserIdList(List<Integer> userIdList) {
        if (userIdList == null || userIdList.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.deleteBatchByUserIdList(userIdList);
    }

    @Override
    public UserInfo getUserInfoByUserNo(String userNo) {
        return this.userInfoMapper.selectByUserNo(userNo);
    }

    @Override
    public Integer updateUserInfoByUserNo(UserInfo bean, String userNo) {
        prepareUserForSave(bean, false);
        return this.userInfoMapper.updateByUserNo(bean, userNo);
    }

    @Override
    public Integer deleteUserInfoByUserNo(String userNo) {
        return this.userInfoMapper.deleteByUserNo(userNo);
    }

    @Override
    public UserInfo getUserInfoByPhone(String phone) {
        return this.userInfoMapper.selectByPhone(phone);
    }

    @Override
    public Integer updateUserInfoByPhone(UserInfo bean, String phone) {
        prepareUserForSave(bean, false);
        return this.userInfoMapper.updateByPhone(bean, phone);
    }

    @Override
    public Integer deleteUserInfoByPhone(String phone) {
        return this.userInfoMapper.deleteByPhone(phone);
    }

    @Override
    public List<Integer> getUsedClassIdList(List<Integer> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            return List.of();
        }
        return this.userInfoMapper.selectUsedClassIdList(classIdList);
    }

    private void prepareUserForSave(UserInfo bean, boolean create) {
        if (bean == null) {
            return;
        }
        bean.setUserNo(trimToNull(bean.getUserNo()));
        bean.setRealName(trimToNull(bean.getRealName()));
        bean.setPhone(trimToNull(bean.getPhone()));
        bean.setEmail(trimToNull(bean.getEmail()));
        bean.setAvatar(trimToNull(bean.getAvatar()));
        bean.setTitleName(trimToNull(bean.getTitleName()));
        if (create && StringTools.isEmpty(bean.getPassword())) {
            bean.setPassword(DEFAULT_PASSWORD);
            return;
        }
        bean.setPassword(trimToNull(bean.getPassword()));
    }

    private String trimToNull(String value) {
        if (StringTools.isEmpty(value)) {
            return null;
        }
        return value.trim();
    }
}
