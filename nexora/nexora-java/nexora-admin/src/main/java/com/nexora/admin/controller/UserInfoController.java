package com.nexora.admin.controller;

import com.nexora.constants.Constants;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.UserInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 用户管理控制器 — 管理端对用户的增删改查
 */
@RestController
@RequestMapping("/userInfo")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<UserInfo>> loadDataList(UserInfoQuery query) {
        PaginationResultVO<UserInfo> result = userInfoService.findListByPage(query);
        return ResponseVO.success(result);
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
        return ResponseVO.success(userInfo);
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public ResponseVO<?> add(@RequestBody UserInfo userInfo) {
        if (StringTools.isEmpty(userInfo.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (StringTools.isEmpty(userInfo.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        // 密码 MD5 加密
        userInfo.setPassword(DigestUtils.md5Hex(userInfo.getPassword()));
        // 设置默认值
        if (userInfo.getStatus() == null) {
            userInfo.setStatus(Constants.STATUS_ENABLE);
        }
        userInfo.setCreateTime(new Date());
        userInfoService.add(userInfo);
        return ResponseVO.success();
    }

    /**
     * 编辑用户
     */
    @PutMapping("/update")
    public ResponseVO<?> update(@RequestBody UserInfo userInfo) {
        if (userInfo.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        // 密码非空时 MD5 加密，空则不更新
        if (!StringTools.isEmpty(userInfo.getPassword())) {
            userInfo.setPassword(DigestUtils.md5Hex(userInfo.getPassword()));
        } else {
            userInfo.setPassword(null);
        }
        userInfo.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(userInfo, userInfo.getUserId());
        return ResponseVO.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/del")
    public ResponseVO<?> del(@RequestParam Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        userInfoService.deleteUserInfoByUserId(userId);
        return ResponseVO.success();
    }

    /**
     * 启用 / 禁用用户
     */
    @PutMapping("/changeStatus")
    public ResponseVO<?> changeStatus(@RequestParam Integer userId, @RequestParam Integer status) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!Constants.STATUS_ENABLE.equals(status) && !Constants.STATUS_DISABLE.equals(status)) {
            throw new BusinessException("非法的状态值");
        }
        UserInfo updateBean = new UserInfo();
        updateBean.setStatus(status);
        updateBean.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, userId);
        return ResponseVO.success();
    }
}
