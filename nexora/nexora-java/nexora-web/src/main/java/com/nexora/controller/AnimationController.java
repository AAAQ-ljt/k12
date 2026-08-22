package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生动画讲解：我的动画列表（个人知识库 ANIMATION 资源）/ 详情 / 删除
 */
@RestController
@RequestMapping("/animation")
@GlobalInterceptor(checkLogin = true)
public class AnimationController extends ABaseController {

    @Resource
    private ResourceInfoService resourceInfoService;

    @GetMapping("/myList")
    public ResponseVO<List<ResourceInfo>> myList() {
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setOwnerId(currentUserId());
        query.setResourceType("ANIMATION");
        query.setOrderBy("create_time desc");
        return getSuccessResponseVO(resourceInfoService.findListByParam(query));
    }

    @GetMapping("/getInfo")
    public ResponseVO<ResourceInfo> getInfo(@RequestParam String resourceId) {
        return getSuccessResponseVO(assertOwned(resourceId));
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String resourceId) {
        assertOwned(resourceId);
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
        return getSuccessResponseVO(null);
    }

    private ResourceInfo assertOwned(String resourceId) {
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || !currentUserId().equals(resource.getOwnerId())) {
            throw new BusinessException("动画不存在或无权操作");
        }
        return resource;
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }
}