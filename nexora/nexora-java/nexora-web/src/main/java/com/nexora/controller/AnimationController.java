package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.dto.AnimationGenerateRequest;
import com.nexora.dto.AnimationTaskVO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.AnimationTaskService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生动画讲解：异步生成任务 / 我的动画列表（个人知识库 ANIMATION 资源）/ 详情 / 删除
 */
@RestController
@RequestMapping("/animation")
@GlobalInterceptor(checkLogin = true)
public class AnimationController extends ABaseController {

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private AnimationTaskService animationTaskService;

    /**
     * 提交动画讲解生成任务：立即返回 taskId，前端轮询 /task 获取进度
     */
    @PostMapping("/generate")
    public ResponseVO<AnimationTaskVO> generate(@RequestBody AnimationGenerateRequest request) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (request == null || StringTools.isEmpty(request.getTopic())) {
            throw new BusinessException("请先输入想讲解的知识点");
        }
        String topic = request.getTopic().trim();
        if (topic.length() > 50) {
            throw new BusinessException("主题请在 50 字以内");
        }
        return getSuccessResponseVO(animationTaskService.submit(
                current.getUserId(), current.getStage(), topic));
    }

    /**
     * 查询动画生成任务状态
     */
    @GetMapping("/task")
    public ResponseVO<AnimationTaskVO> task(@RequestParam String taskId) {
        return getSuccessResponseVO(animationTaskService.get(currentUserId(), taskId));
    }

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