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
}
