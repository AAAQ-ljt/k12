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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<String> findSubTreeDirIds(String rootDirId, String ownerId) {
        if (StringTools.isEmpty(rootDirId)) {
            return new ArrayList<>();
        }
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        if (StringTools.isEmpty(ownerId)) {
            query.setOwnerIdNull(Boolean.TRUE);
        } else {
            query.setOwnerId(ownerId);
        }
        // 目录全量一次取回（管理端 30 余条、个人目录更少），内存建父子索引后展开子树
        List<ResourceDirectory> all = resourceDirectoryMapper.selectList(query);
        Map<String, List<String>> childrenMap = new HashMap<>();
        for (ResourceDirectory directory : all) {
            childrenMap.computeIfAbsent(directory.getParentId(), key -> new ArrayList<>())
                    .add(directory.getDirId());
        }
        List<String> result = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(rootDirId);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            result.add(current);
            List<String> children = childrenMap.get(current);
            if (children != null) {
                for (String child : children) {
                    stack.push(child);
                }
            }
        }
        return result;
    }

    @Override
    public Integer updateSortBatch(List<ResourceDirectory> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return resourceDirectoryMapper.updateSortBatch(list);
    }
}
