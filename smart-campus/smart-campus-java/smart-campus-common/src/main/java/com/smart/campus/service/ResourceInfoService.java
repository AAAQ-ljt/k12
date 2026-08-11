package com.smart.campus.service;

import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.query.ResourceInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;

import java.util.List;

public interface ResourceInfoService {

    List<ResourceInfo> findListByParam(ResourceInfoQuery param);

    Integer findCountByParam(ResourceInfoQuery param);

    PaginationResultVO<ResourceInfo> findListByPage(ResourceInfoQuery param);

    Integer add(ResourceInfo bean);

    Integer addBatch(List<ResourceInfo> listBean);

    Integer addOrUpdateBatch(List<ResourceInfo> listBean);

    Integer updateByParam(ResourceInfo bean, ResourceInfoQuery param);

    Integer deleteByParam(ResourceInfoQuery param);

    ResourceInfo getResourceInfoByResourceId(Integer resourceId);

    Integer updateResourceInfoByResourceId(ResourceInfo bean, Integer resourceId);

    Integer deleteResourceInfoByResourceId(Integer resourceId);

    List<ResourceInfo> getResourceInfoByResourceIdList(List<String> resourceIdList);
}
