package com.nexora.service;

import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 资源目录表 业务接口
 */
public interface ResourceDirectoryService {

    List<ResourceDirectory> findListByParam(ResourceDirectoryQuery param);

    Integer findCountByParam(ResourceDirectoryQuery param);

    PaginationResultVO<ResourceDirectory> findListByPage(ResourceDirectoryQuery param);

    Integer add(ResourceDirectory bean);

    Integer updateByParam(ResourceDirectory bean, ResourceDirectoryQuery param);

    Integer deleteByParam(ResourceDirectoryQuery param);

    ResourceDirectory getResourceDirectoryByDirId(String dirId);

    Integer updateResourceDirectoryByDirId(ResourceDirectory bean, String dirId);

    Integer deleteResourceDirectoryByDirId(String dirId);

    Integer updateSortBatch(List<ResourceDirectory> list);

    /**
     * 收集「目录自身 + 全部子孙目录」的 dirId（文件列表按目录子树过滤用）。
     * 目录一次性取回后在内存回溯（禁止循环查库）。
     *
     * @param rootDirId 根目录ID
     * @param ownerId   目录归属：空串 / null = 管理端公共目录（owner_id IS NULL），非空 = 该学生的个人目录
     */
    List<String> findSubTreeDirIds(String rootDirId, String ownerId);
}
