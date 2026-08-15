package com.nexora.service.impl;

import com.nexora.constants.Constants;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.service.ResourceDirectoryService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentKnowledgeBaseService;
import com.nexora.utils.StringTools;
import com.nexora.vo.StudentStorageVO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 学生个人知识库业务实现：默认目录初始化 + 存储额度
 */
@Service("studentKnowledgeBaseService")
public class StudentKnowledgeBaseServiceImpl implements StudentKnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(StudentKnowledgeBaseServiceImpl.class);

    private static final List<String> DEFAULT_DIR_NAMES = List.of("我的文档", "我的图片", "其他");

    @Value("${resource.student-quota-mb:300}")
    private long studentQuotaMb;

    @Resource
    private ResourceDirectoryService resourceDirectoryService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Override
    public boolean initIfAbsent(String ownerId) {
        if (StringTools.isEmpty(ownerId)) {
            return false;
        }
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerId(ownerId);
        if (resourceDirectoryService.findCountByParam(query) > 0) {
            return false;
        }
        try {
            int sort = 1;
            Date now = new Date();
            for (String dirName : DEFAULT_DIR_NAMES) {
                ResourceDirectory dir = new ResourceDirectory();
                dir.setDirId(StringTools.getRandomNumber(Constants.LENGTH_15));
                dir.setDirName(dirName);
                dir.setParentId("0");
                dir.setOwnerId(ownerId);
                dir.setSort(sort++);
                dir.setCreateTime(now);
                dir.setUpdateTime(now);
                resourceDirectoryService.add(dir);
            }
            return true;
        } catch (Exception e) {
            log.error("初始化学生个人知识库失败 ownerId={}", ownerId, e);
            return false;
        }
    }

    @Override
    public StudentStorageVO getStorageInfo(String ownerId) {
        long quotaBytes = studentQuotaMb * 1024 * 1024;
        Long used = resourceInfoService.getUsedSizeByOwner(ownerId);
        long usedBytes = used == null ? 0L : used;

        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerId(ownerId);
        boolean initialized = resourceDirectoryService.findCountByParam(query) > 0;

        StudentStorageVO vo = new StudentStorageVO();
        vo.setUsedBytes(usedBytes);
        vo.setQuotaBytes(quotaBytes);
        vo.setRemainingBytes(Math.max(0L, quotaBytes - usedBytes));
        vo.setInitialized(initialized);
        return vo;
    }
}
