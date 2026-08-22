package com.nexora.service;

import com.nexora.entity.po.UserWikiProfile;
import com.nexora.entity.query.UserWikiProfileQuery;
import com.nexora.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 学生学习档案 业务接口
 */
public interface UserWikiProfileService {

    List<UserWikiProfile> findListByParam(UserWikiProfileQuery param);

    Integer findCountByParam(UserWikiProfileQuery param);

    PaginationResultVO<UserWikiProfile> findListByPage(UserWikiProfileQuery param);

    Integer add(UserWikiProfile bean);

    Integer updateByParam(UserWikiProfile bean, UserWikiProfileQuery param);

    Integer deleteByParam(UserWikiProfileQuery param);

    UserWikiProfile getUserWikiProfileByUserId(String userId);

    Integer updateUserWikiProfileByUserId(UserWikiProfile bean, String userId);

    Integer deleteUserWikiProfileByUserId(String userId);
}