package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.TtsProvider;
import com.nexora.dto.PictureBookAudioRequest;
import com.nexora.dto.PictureBookAudioTaskVO;
import com.nexora.dto.PictureBookGenerateRequest;
import com.nexora.dto.PictureBookPageFixRequest;
import com.nexora.dto.PictureBookPageFixVO;
import com.nexora.dto.PictureBookTaskVO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.PictureBookService;
import com.nexora.service.PictureBookTaskService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 学生绘本 Controller：提交异步生成任务 / 查询任务 / 列表 / 详情 / 删除 / 页内插图
 */
@RestController
@RequestMapping("/pictureBook")
@GlobalInterceptor(checkLogin = true)
public class PictureBookController extends ABaseController {

    @Resource
    private PictureBookService pictureBookService;

    @Resource
    private PictureBookTaskService pictureBookTaskService;

    @Value("${project.folder}")
    private String projectFolder;

    /**
     * 提交绘本生成任务：立即返回 taskId，前端轮询 /task 获取进度（异步编排，不再阻塞等待）
     */
    @PostMapping("/generate")
    public ResponseVO<PictureBookTaskVO> generate(@RequestBody PictureBookGenerateRequest request) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (request == null || StringTools.isEmpty(request.getTopic())) {
            throw new BusinessException("请先输入绘本主题");
        }
        if (request.getVoice() != null && !request.getVoice().isBlank()
                && !TtsProvider.isValidVoice(request.getVoice().trim())) {
            throw new BusinessException("不支持的音色");
        }
        return getSuccessResponseVO(pictureBookTaskService.submit(
                current.getUserId(), current.getStage(), request.getTopic().trim(),
                request.getVoice() == null ? null : request.getVoice().trim()));
    }

    /**
     * 查询绘本生成任务状态
     */
    @GetMapping("/task")
    public ResponseVO<PictureBookTaskVO> task(@RequestParam String taskId) {
        return getSuccessResponseVO(pictureBookTaskService.get(currentUserId(), taskId));
    }

    @GetMapping("/myList")
    public ResponseVO<List<ResourceInfo>> myList() {
        return getSuccessResponseVO(pictureBookService.myList(currentUserId()));
    }

    @GetMapping("/getInfo")
    public ResponseVO<ResourceInfo> getInfo(@RequestParam String resourceId) {
        return getSuccessResponseVO(pictureBookService.getInfo(currentUserId(), resourceId));
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String resourceId) {
        pictureBookService.delete(currentUserId(), resourceId);
        return getSuccessResponseVO(null);
    }

    /**
     * 提交指定页补画任务（异步）：对已生成绘本中没有插图/插画丢失的页手动补画，前端轮询 /pageFixTask 获取状态
     */
    @PostMapping("/regeneratePage")
    public ResponseVO<PictureBookPageFixVO> regeneratePage(@RequestBody PictureBookPageFixRequest request) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (request == null || StringTools.isEmpty(request.getResourceId()) || request.getPage() == null) {
            throw new BusinessException("补画参数不完整");
        }
        return getSuccessResponseVO(pictureBookService.submitPageFix(
                current.getUserId(), request.getResourceId(), request.getPage()));
    }

    /**
     * 查询指定页补画任务状态
     */
    @GetMapping("/pageFixTask")
    public ResponseVO<PictureBookPageFixVO> pageFixTask(@RequestParam String resourceId, @RequestParam Integer page) {
        return getSuccessResponseVO(pictureBookService.getPageFix(currentUserId(), resourceId, page));
    }

    /**
     * 绘本指定页插图：与现有媒体接口一致，公开直连（WebMvcConfig 已排除拦截）
     */
    @GetMapping("/image/{resourceId}")
    @GlobalInterceptor(checkLogin = false)
    public ResponseEntity<FileSystemResource> pageImage(@PathVariable String resourceId,
                                                        @RequestParam(defaultValue = "0") int page) {
        String imageFile = pictureBookService.pageImageFile(resourceId, page);
        if (StringTools.isEmpty(imageFile)) {
            throw new BusinessException("插图不存在");
        }
        Path path = Paths.get(projectFolder, imageFile);
        if (!Files.exists(path)) {
            throw new BusinessException("插图文件不存在");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(path.toFile()));
    }

    /**
     * 提交旁白合成任务（异步）：page 为空=整本补录（跳过同音色已合成页），page 给定=单页（重）录制；
     * voice 为用户自选音色；前端轮询 /audioTask 获取状态
     */
    @PostMapping("/generateAudio")
    public ResponseVO<PictureBookAudioTaskVO> generateAudio(@RequestBody PictureBookAudioRequest request) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (request == null || StringTools.isEmpty(request.getResourceId())) {
            throw new BusinessException("旁白录制参数不完整");
        }
        return getSuccessResponseVO(pictureBookService.submitAudioTask(
                current.getUserId(), request.getResourceId(), request.getPage(), request.getVoice()));
    }

    /**
     * 查询旁白合成任务状态
     */
    @GetMapping("/audioTask")
    public ResponseVO<PictureBookAudioTaskVO> audioTask(@RequestParam String taskId) {
        return getSuccessResponseVO(pictureBookService.getAudioTask(currentUserId(), taskId));
    }

    /**
     * 绘本指定页旁白音频：与插图一致公开直连（WebMvcConfig 已排除拦截）
     */
    @GetMapping("/audio/{resourceId}")
    @GlobalInterceptor(checkLogin = false)
    public ResponseEntity<FileSystemResource> pageAudio(@PathVariable String resourceId,
                                                        @RequestParam(defaultValue = "0") int page) {
        String audioFile = pictureBookService.pageAudioFile(resourceId, page);
        if (StringTools.isEmpty(audioFile)) {
            throw new BusinessException("旁白音频不存在");
        }
        Path path = Paths.get(projectFolder, audioFile);
        if (!Files.exists(path)) {
            throw new BusinessException("旁白音频文件不存在");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(new FileSystemResource(path.toFile()));
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }
}