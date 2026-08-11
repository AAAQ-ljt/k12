package com.smart.campus.service;

import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;

import java.util.List;

public interface ClassInfoService {

    List<ClassInfo> findListByParam(ClassInfoQuery param);

    Integer findCountByParam(ClassInfoQuery param);

    PaginationResultVO<ClassInfo> findListByPage(ClassInfoQuery param);

    Integer add(ClassInfo bean);

    Integer addBatch(List<ClassInfo> listBean);

    Integer addOrUpdateBatch(List<ClassInfo> listBean);

    Integer updateByParam(ClassInfo bean, ClassInfoQuery param);

    Integer deleteByParam(ClassInfoQuery param);

    ClassInfo getClassInfoByClassId(Integer classId);

    Integer updateClassInfoByClassId(ClassInfo bean, Integer classId);

    Integer deleteClassInfoByClassId(Integer classId);

    List<ClassInfo> getClassInfoByClassIdList(List<Integer> classIdList);

    Integer deleteBatchByClassIdList(List<Integer> classIdList);

    Integer updateSortOrderBatch(List<ClassInfo> list);
}
