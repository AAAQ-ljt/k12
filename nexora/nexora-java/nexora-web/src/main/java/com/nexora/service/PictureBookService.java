package com.nexora.service;

import com.nexora.entity.po.ResourceInfo;
import com.nexora.vo.PictureBookVO;

import java.util.List;

/**
 * 学生绘本业务：生成绘本（故事 + 插图，产物存 resource_info.ext_json）、列表、详情、删除
 */
public interface PictureBookService {

    /**
     * 生成绘本：LLM 故事分页文案 + 逐页文生图；单页插图失败降级为纯文字页
     */
    PictureBookVO generate(String userId, String stage, String topic);

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
}