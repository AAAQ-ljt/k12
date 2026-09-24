package com.nexora.service;

import com.nexora.dto.PictureBookPageFixVO;
import com.nexora.entity.po.ResourceInfo;

import java.util.List;

/**
 * 学生绘本业务：生成由异步任务编排（PictureBookTaskConsumer），此处负责产物落库、列表、详情、删除、单页补画
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
     * 删除绘本（级联删除产物图片文件）
     */
    void delete(String userId, String resourceId);

    /**
     * 绘本指定页插图文件相对路径（公开直连接口使用）
     */
    String pageImageFile(String resourceId, int pageIndex);

    /**
     * 提交指定页补画任务（异步，resourceId:page 运行锁防重复；返回任务状态供轮询）
     */
    PictureBookPageFixVO submitPageFix(String userId, String resourceId, int page);

    /**
     * 查询指定页补画任务状态
     */
    PictureBookPageFixVO getPageFix(String userId, String resourceId, int page);
}