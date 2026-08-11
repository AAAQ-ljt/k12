package com.smart.campus.service;

import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.query.MajorInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 专业表 业务接口
 */
public interface MajorInfoService {

    List<MajorInfo> findListByParam(MajorInfoQuery param);

    Integer findCountByParam(MajorInfoQuery param);

    PaginationResultVO<MajorInfo> findListByPage(MajorInfoQuery param);

    Integer add(MajorInfo bean);

    Integer addBatch(List<MajorInfo> listBean);

    Integer addOrUpdateBatch(List<MajorInfo> listBean);

    Integer updateByParam(MajorInfo bean, MajorInfoQuery param);

    Integer deleteByParam(MajorInfoQuery param);

    MajorInfo getMajorInfoByMajorId(Integer majorId);

    Integer updateMajorInfoByMajorId(MajorInfo bean, Integer majorId);

    Integer deleteMajorInfoByMajorId(Integer majorId);

    MajorInfo getMajorInfoByMajorCode(String majorCode);

    Integer updateMajorInfoByMajorCode(MajorInfo bean, String majorCode);

    Integer deleteMajorInfoByMajorCode(String majorCode);

    List<MajorInfo> getMajorInfoByMajorIdList(List<Integer> majorIdList);

    Integer updateSortOrderBatch(List<MajorInfo> list);
}
