package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.query.ResourceInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.ResourceInfoMapper;
import com.smart.campus.service.ResourceInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service("resourceInfoService")
public class ResourceInfoServiceImpl implements ResourceInfoService {

    @Resource
    private ResourceInfoMapper<ResourceInfo, ResourceInfoQuery> resourceInfoMapper;

    @Override
    public List<ResourceInfo> findListByParam(ResourceInfoQuery param) {
        return resourceInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ResourceInfoQuery param) {
        return resourceInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ResourceInfo> findListByPage(ResourceInfoQuery param) {
        int count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ResourceInfo> list = findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(ResourceInfo bean) {
        return resourceInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<ResourceInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return resourceInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<ResourceInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return resourceInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(ResourceInfo bean, ResourceInfoQuery param) {
        StringTools.checkParam(param);
        return resourceInfoMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(ResourceInfoQuery param) {
        StringTools.checkParam(param);
        return resourceInfoMapper.deleteByParam(param);
    }

    @Override
    public ResourceInfo getResourceInfoByResourceId(Integer resourceId) {
        return resourceInfoMapper.selectByResourceId(resourceId);
    }

    @Override
    public Integer updateResourceInfoByResourceId(ResourceInfo bean, Integer resourceId) {
        return resourceInfoMapper.updateByResourceId(bean, resourceId);
    }

    @Override
    public Integer deleteResourceInfoByResourceId(Integer resourceId) {
        return resourceInfoMapper.deleteByResourceId(resourceId);
    }

    @Override
    public List<ResourceInfo> getResourceInfoByResourceIdList(List<String> resourceIdList) {
        if (resourceIdList == null || resourceIdList.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> normalizedIdList = resourceIdList.stream()
                .map(StringTools::trim)
                .filter(value -> !StringTools.isEmpty(value))
                .map(value -> {
                    try {
                        return Integer.valueOf(value);
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
        if (normalizedIdList.isEmpty()) {
            return Collections.emptyList();
        }
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setResourceIdList(normalizedIdList);
        return resourceInfoMapper.selectList(query);
    }
}
