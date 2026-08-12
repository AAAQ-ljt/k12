package com.nexora.admin.controller;

import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.UserInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 用户管理 Controller — 管理端对用户的增删改查
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 分页查询用户列表（email / stage / status / roleType 筛选）
     */
    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<UserInfo>> loadDataList(UserInfoQuery query) {
        return getSuccessResponseVO(userInfoService.findListByPage(query));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/getInfo")
    public ResponseVO<UserInfo> getInfo(@RequestParam Integer userId) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo == null) {
            throw new BusinessException("用户不存在");
        }
        return getSuccessResponseVO(userInfo);
    }

    /**
     * 新增用户（密码 MD5，默认启用）
     */
    @PostMapping("/add")
    public ResponseVO<Void> add(@RequestBody UserInfo userInfo) {
        if (StringTools.isEmpty(userInfo.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        // 密码缺省默认 123456（MD5 存储），前端新增用户不填密码
        if (StringTools.isEmpty(userInfo.getPassword())) {
            userInfo.setPassword("123456");
        }
        userInfo.setPassword(StringTools.encodeByMD5(userInfo.getPassword()));
        // 默认值：启用、创建时间
        if (userInfo.getStatus() == null) {
            userInfo.setStatus(Constants.STATUS_ENABLE);
        }
        // 年级 -> 学段兜底：传了年级但学段为空时自动匹配
        if (StringTools.isEmpty(userInfo.getStage()) && !StringTools.isEmpty(userInfo.getGrade())) {
            userInfo.setStage(StageEnum.matchByGrade(userInfo.getGrade()));
        }
        userInfo.setCreateTime(new Date());
        userInfoService.add(userInfo);
        return getSuccessResponseVO(null);
    }

    /**
     * 编辑用户（password 为空则不修改）
     */
    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody UserInfo userInfo) {
        if (userInfo.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringTools.isEmpty(userInfo.getPassword())) {
            userInfo.setPassword(StringTools.encodeByMD5(userInfo.getPassword()));
        } else {
            userInfo.setPassword(null);
        }
        // 年级 -> 学段兜底：传了年级且学段为空时自动匹配
        if (!StringTools.isEmpty(userInfo.getGrade()) && StringTools.isEmpty(userInfo.getStage())) {
            userInfo.setStage(StageEnum.matchByGrade(userInfo.getGrade()));
        }
        userInfo.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(userInfo, userInfo.getUserId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam Integer userId) {
        userInfoService.deleteUserInfoByUserId(userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 启用 / 禁用用户
     */
    @PutMapping("/changeStatus")
    public ResponseVO<Void> changeStatus(@RequestParam Integer userId, @RequestParam Integer status) {
        if (!Constants.STATUS_ENABLE.equals(status) && !Constants.STATUS_DISABLE.equals(status)) {
            throw new BusinessException("非法的状态值");
        }
        UserInfo updateBean = new UserInfo();
        updateBean.setStatus(status);
        updateBean.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, userId);
        return getSuccessResponseVO(null);
    }
}
