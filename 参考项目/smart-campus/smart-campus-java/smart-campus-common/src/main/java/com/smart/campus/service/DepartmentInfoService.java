package com.smart.campus.service;

import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.query.DepartmentInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 院系表 业务接口
 */
public interface DepartmentInfoService {

    List<DepartmentInfo> findListByParam(DepartmentInfoQuery param);

    Integer findCountByParam(DepartmentInfoQuery param);

    PaginationResultVO<DepartmentInfo> findListByPage(DepartmentInfoQuery param);

    Integer add(DepartmentInfo bean);

    Integer addBatch(List<DepartmentInfo> listBean);

    Integer addOrUpdateBatch(List<DepartmentInfo> listBean);

    Integer updateByParam(DepartmentInfo bean, DepartmentInfoQuery param);

    Integer deleteByParam(DepartmentInfoQuery param);

    DepartmentInfo getDepartmentInfoByDepartmentId(Integer departmentId);

    Integer updateDepartmentInfoByDepartmentId(DepartmentInfo bean, Integer departmentId);

    Integer deleteDepartmentInfoByDepartmentId(Integer departmentId);

    DepartmentInfo getDepartmentInfoByDepartmentCode(String departmentCode);

    Integer updateDepartmentInfoByDepartmentCode(DepartmentInfo bean, String departmentCode);

    Integer deleteDepartmentInfoByDepartmentCode(String departmentCode);

    List<DepartmentInfo> getDepartmentInfoByDepartmentIdList(List<Integer> departmentIdList);

    Integer deleteBatchByDepartmentIdList(List<Integer> departmentIdList);

    Integer updateSortOrderBatch(List<DepartmentInfo> list);
}
