package com.nexora.service.impl;

import com.nexora.constants.Constants;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.exception.BusinessException;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 学生个人知识库业务实现：三层系统目录（raw/wiki/attachments）初始化、存储额度、上传归类
 */
@Service("studentKnowledgeBaseService")
public class StudentKnowledgeBaseServiceImpl implements StudentKnowledgeBaseService {

    private static final Logger log = LoggerFactory.getLogger(StudentKnowledgeBaseServiceImpl.class);

    /** 系统目录定义：dirType -> 展示名称（顺序即初始化排序） */
    private static final Map<String, String> SYSTEM_DIRECTORIES = new LinkedHashMap<>() {{
        put(DIR_TYPE_RAW, "原始资料");
        put(DIR_TYPE_WIKI, "知识页");
        put(DIR_TYPE_ATTACHMENTS, "附件");
    }};

    private static final List<String> RAW_DOCUMENT_EXTENSIONS = List.of("md", "txt");

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
            createSystemDirectories(ownerId);
            return true;
        } catch (Exception e) {
            log.error("初始化学生个人知识库失败 ownerId={}", ownerId, e);
            return false;
        }
    }

    @Override
    public StudentStorageVO getStorageInfo(String ownerId) {
        ensureSystemDirectories(ownerId);
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

    @Override
    public void ensureSystemDirectories(String ownerId) {
        if (StringTools.isEmpty(ownerId)) {
            return;
        }
        try {
            List<ResourceDirectory> existing = listDirectories(ownerId);
            int sort = 1;
            for (Map.Entry<String, String> entry : SYSTEM_DIRECTORIES.entrySet()) {
                boolean present = existing.stream()
                        .anyMatch(dir -> entry.getKey().equals(dir.getDirType()));
                if (!present) {
                    createDirectory(ownerId, entry.getValue(), entry.getKey(), sort);
                }
                sort++;
            }
        } catch (Exception e) {
            log.warn("补齐学生系统目录失败 ownerId={}", ownerId, e);
        }
    }

    @Override
    public ResourceDirectory getSystemDirectory(String ownerId, String dirType) {
        if (StringTools.isEmpty(ownerId) || StringTools.isEmpty(dirType)) {
            return null;
        }
        return listDirectories(ownerId).stream()
                .filter(dir -> dirType.equals(dir.getDirType()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String resolveDefaultDirectoryId(String ownerId, String resourceType, String extension) {
        if (StringTools.isEmpty(ownerId)) {
            return null;
        }
        String ext = normalizeExtension(extension);
        boolean rawDoc = "DOCUMENT".equalsIgnoreCase(resourceType) && RAW_DOCUMENT_EXTENSIONS.contains(ext);
        String dirType = rawDoc ? DIR_TYPE_RAW : DIR_TYPE_ATTACHMENTS;
        ResourceDirectory dir = getSystemDirectory(ownerId, dirType);
        return dir == null ? null : dir.getDirId();
    }

    @Override
    public void validateDirectoryState(String ownerId, String directoryId, String resourceType, String extension) {
        if (StringTools.isEmpty(directoryId)) {
            return;
        }
        ResourceDirectory directory = getOwnedDirectory(ownerId, directoryId);
        if (directory == null || StringTools.isEmpty(directory.getDirType())) {
            return;
        }
        if (DIR_TYPE_RAW.equals(directory.getDirType())
                && !(RAW_DOCUMENT_EXTENSIONS.contains(normalizeExtension(extension)))) {
            throw new BusinessException("「原始资料」目录仅支持 md/txt 文档，请上传到「附件」或自定义目录");
        }
    }

    /**
     * 判断目录是否为系统目录（禁止改名/删除/挂普通子目录）
     */
    public static boolean isSystemDirectory(String dirType) {
        return !StringTools.isEmpty(dirType);
    }

    private List<ResourceDirectory> listDirectories(String ownerId) {
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerId(ownerId);
        query.setOrderBy("parent_id asc, sort asc");
        return resourceDirectoryService.findListByParam(query);
    }

    private ResourceDirectory getOwnedDirectory(String ownerId, String directoryId) {
        ResourceDirectory directory = resourceDirectoryService.getResourceDirectoryByDirId(directoryId);
        if (directory == null || !ownerId.equals(directory.getOwnerId())) {
            throw new BusinessException("目录不存在或无权操作");
        }
        return directory;
    }

    private void createSystemDirectories(String ownerId) {
        int sort = 1;
        for (Map.Entry<String, String> entry : SYSTEM_DIRECTORIES.entrySet()) {
            createDirectory(ownerId, entry.getValue(), entry.getKey(), sort++);
        }
    }

    private void createDirectory(String ownerId, String dirName, String dirType, int sort) {
        Date now = new Date();
        ResourceDirectory dir = new ResourceDirectory();
        dir.setDirId(StringTools.getRandomNumber(Constants.LENGTH_15));
        dir.setDirName(dirName);
        dir.setParentId("0");
        dir.setDirType(dirType);
        dir.setOwnerId(ownerId);
        dir.setSort(sort);
        dir.setCreateTime(now);
        dir.setUpdateTime(now);
        resourceDirectoryService.add(dir);
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        String ext = extension.trim().toLowerCase(Locale.ROOT);
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }
        return ext;
    }
}