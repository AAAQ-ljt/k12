package com.nexora.service;

import com.nexora.dto.PictureBookAudioTaskVO;
import com.nexora.dto.PictureBookPageFixVO;
import com.nexora.entity.po.ResourceInfo;

import java.util.List;

/**
 * 学生绘本业务：生成由异步任务编排（PictureBookTaskConsumer），此处负责产物落库、列表、详情、删除、单页补画、旁白合成任务
 */
public interface PictureBookService {

    /**
     * 保存绘本产物到资源中心（PICTURE_BOOK 类型，附件目录，owner 隔离）
     */
    ResourceInfo saveBook(String userId, String stage, String title, String extJson);

    /**
     * 我的绘本列表
     */
    List<ResourceInfo> myList(String userId);

    /**
     * 绘本详情
     */
    ResourceInfo getInfo(String userId, String resourceId);

    /**
     * 删除绘本（级联删除产物图片与旁白音频文件）
     */
    void delete(String userId, String resourceId);

    /**
     * 绘本指定页插图文件相对路径（公开直连接口使用）
     */
    String pageImageFile(String resourceId, int pageIndex);

    /**
     * 绘本指定页旁白音频文件相对路径（公开直连接口使用）
     */
    String pageAudioFile(String resourceId, int pageIndex);

    /**
     * 提交指定页补画任务（异步，resourceId:page 运行锁防重复；返回任务状态供轮询）
     */
    PictureBookPageFixVO submitPageFix(String userId, String resourceId, int page);

    /**
     * 查询指定页补画任务状态
     */
    PictureBookPageFixVO getPageFix(String userId, String resourceId, int page);

    /**
     * 提交旁白合成任务（异步）：page 为空=整本补录（跳过同音色已合成页，异音色页重录覆盖），
     * page 给定=单页（重）录制；voice 为用户自选音色（白名单校验，空回落学段默认）。
     * resourceId 粒度运行锁，同一本书同时只跑一个语音任务
     */
    PictureBookAudioTaskVO submitAudioTask(String userId, String resourceId, Integer page, String voice);

    /**
     * 查询旁白合成任务状态
     */
    PictureBookAudioTaskVO getAudioTask(String userId, String taskId);
}