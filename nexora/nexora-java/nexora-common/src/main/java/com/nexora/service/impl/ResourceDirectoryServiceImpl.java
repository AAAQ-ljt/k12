package com.nexora.service.impl;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.ResourceDirectoryMapper;
import com.nexora.service.ResourceDirectoryService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 资源目录表 业务接口实现
 */
@Service("resourceDirectoryService")
public class ResourceDirectoryServiceImpl implements ResourceDirectoryService {

    @Resource
    private ResourceDirectoryMapper<ResourceDirectory, ResourceDirectoryQuery> resourceDirectoryMapper;

    @Override
    public List<ResourceDirectory> findListByParam(ResourceDirectoryQuery param) {
        return resourceDirectoryMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ResourceDirectoryQuery param) {
        return resourceDirectoryMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ResourceDirectory> findListByPage(ResourceDirectoryQuery param) {
        int count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), findListByParam(param));
    }

    @Override
    public Integer add(ResourceDirectory bean) {
        return resourceDirectoryMapper.insert(bean);
    }

    @Override
    public Integer updateByParam(ResourceDirectory bean, ResourceDirectoryQuery param) {
        StringTools.checkParam(param);
        return resourceDirectoryMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(ResourceDirectoryQuery param) {
        StringTools.checkParam(param);
        return resourceDirectoryMapper.deleteByParam(param);
    }

    @Override
    public ResourceDirectory getResourceDirectoryByDirId(String dirId) {
        return resourceDirectoryMapper.selectByDirId(dirId);
    }

    @Override
    public Integer updateResourceDirectoryByDirId(ResourceDirectory bean, String dirId) {
        return resourceDirectoryMapper.updateByDirId(bean, dirId);
    }

    @Override
    public Integer deleteResourceDirectoryByDirId(String dirId) {
        return resourceDirectoryMapper.deleteByDirId(dirId);
    }

    @Override
    public Integer updateSortBatch(List<ResourceDirectory> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return resourceDirectoryMapper.updateSortBatch(list);
    }
}
