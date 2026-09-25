package com.nexora.admin.biz;

import com.nexora.component.RedisComponent;
import com.nexora.component.TokenManager;
import com.nexora.constants.Constants;
import com.nexora.entity.po.UserInfo;
import com.nexora.exception.BusinessException;
import com.nexora.service.UserInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 用户管理业务：注册审核与启用/禁用的编排。
 *
 * 两类状态各管一段：
 * - audit_status：注册审核，决定账号能不能进站使用（0待审核 / 1已通过 / 2已驳回）；
 * - status：账号是否停用（0禁用 / 1启用），与审核互不影响。
 *
 * 审核不通过或停用时立即清掉该用户的登录态，避免已登录的 token 继续访问。
 */
@Service
public class UserInfoAdminBiz {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private TokenManager tokenManager;

    /**
     * 注册审核：1 通过 / 2 驳回（驳回同时踢下线）
     */
    public void audit(String userId, Integer auditStatus) {
        if (!Constants.AUDIT_PASSED.equals(auditStatus) && !Constants.AUDIT_REJECTED.equals(auditStatus)) {
            throw new BusinessException("非法的审核状态值");
        }
        if (userInfoService.getUserInfoByUserId(userId) == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfo updateBean = new UserInfo();
        updateBean.setAuditStatus(auditStatus);
        updateBean.setAuditTime(new Date());
        updateBean.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, userId);
        if (!Constants.AUDIT_PASSED.equals(auditStatus)) {
            kickOut(userId);
        }
    }

    /**
     * 启用 / 禁用用户（禁用同时踢下线）
     */
    public void changeStatus(String userId, Integer status) {
        if (!Constants.STATUS_ENABLE.equals(status) && !Constants.STATUS_DISABLE.equals(status)) {
            throw new BusinessException("非法的状态值");
        }
        UserInfo updateBean = new UserInfo();
        updateBean.setStatus(status);
        updateBean.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, userId);
        if (Constants.STATUS_DISABLE.equals(status)) {
            kickOut(userId);
        }
    }

    /** 清掉该用户的登录态：token 由 userId 派生，同一用户全局只有一条登录记录 */
    private void kickOut(String userId) {
        redisComponent.removeToken(tokenManager.generateToken(userId));
    }
}
