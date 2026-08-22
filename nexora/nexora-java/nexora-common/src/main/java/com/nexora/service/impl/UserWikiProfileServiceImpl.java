package com.nexora.service.impl;

import com.nexora.entity.po.UserWikiProfile;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.query.UserWikiProfileQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.UserWikiProfileMapper;
import com.nexora.service.UserWikiProfileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生学习档案 业务实现
 */
@Service("userWikiProfileService")
public class UserWikiProfileServiceImpl implements UserWikiProfileService {

    @Resource
    private UserWikiProfileMapper<UserWikiProfile, UserWikiProfileQuery> userWikiProfileMapper;

    @Override
    public List<UserWikiProfile> findListByParam(UserWikiProfileQuery param) {
        return userWikiProfileMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(UserWikiProfileQuery param) {
        return userWikiProfileMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<UserWikiProfile> findListByPage(UserWikiProfileQuery param) {
        Integer count = findCountByParam(param);
        Integer pageSize = param.getPageSize() == null ? 10 : param.getPageSize();
        int pageNo = param.getPageNo() == null ? 1 : param.getPageNo();
        SimplePage page = new SimplePage(pageNo, count, pageSize);
        param.setSimplePage(page);
        List<UserWikiProfile> list = findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(UserWikiProfile bean) {
        return userWikiProfileMapper.insert(bean);
    }

    @Override
    public Integer updateByParam(UserWikiProfile bean, UserWikiProfileQuery param) {
        return userWikiProfileMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(UserWikiProfileQuery param) {
        return userWikiProfileMapper.deleteByParam(param);
    }

    @Override
    public UserWikiProfile getUserWikiProfileByUserId(String userId) {
        return userWikiProfileMapper.selectByUserId(userId);
    }

    @Override
    public Integer updateUserWikiProfileByUserId(UserWikiProfile bean, String userId) {
        return userWikiProfileMapper.updateByUserId(bean, userId);
    }

    @Override
    public Integer deleteUserWikiProfileByUserId(String userId) {
        return userWikiProfileMapper.deleteByUserId(userId);
    }
}