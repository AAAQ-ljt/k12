package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.query.DepartmentInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.DepartmentInfoMapper;
import com.smart.campus.service.DepartmentInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 院系表 业务接口实现
 */
@Service("departmentInfoService")
public class DepartmentInfoServiceImpl implements DepartmentInfoService {

    @Resource
    private DepartmentInfoMapper<DepartmentInfo, DepartmentInfoQuery> departmentInfoMapper;

    @Override
    public List<DepartmentInfo> findListByParam(DepartmentInfoQuery param) {
        return this.departmentInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(DepartmentInfoQuery param) {
        return this.departmentInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<DepartmentInfo> findListByPage(DepartmentInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<DepartmentInfo> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(DepartmentInfo bean) {
        return this.departmentInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<DepartmentInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.departmentInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<DepartmentInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.departmentInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(DepartmentInfo bean, DepartmentInfoQuery param) {
        StringTools.checkParam(param);
        return this.departmentInfoMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(DepartmentInfoQuery param) {
        StringTools.checkParam(param);
        return this.departmentInfoMapper.deleteByParam(param);
    }

    @Override
    public DepartmentInfo getDepartmentInfoByDepartmentId(Integer departmentId) {
        return this.departmentInfoMapper.selectByDepartmentId(departmentId);
    }

    @Override
    public Integer updateDepartmentInfoByDepartmentId(DepartmentInfo bean, Integer departmentId) {
        return this.departmentInfoMapper.updateByDepartmentId(bean, departmentId);
    }

    @Override
    public Integer deleteDepartmentInfoByDepartmentId(Integer departmentId) {
        return this.departmentInfoMapper.deleteByDepartmentId(departmentId);
    }

    @Override
    public DepartmentInfo getDepartmentInfoByDepartmentCode(String departmentCode) {
        return this.departmentInfoMapper.selectByDepartmentCode(departmentCode);
    }

    @Override
    public Integer updateDepartmentInfoByDepartmentCode(DepartmentInfo bean, String departmentCode) {
        return this.departmentInfoMapper.updateByDepartmentCode(bean, departmentCode);
    }

    @Override
    public Integer deleteDepartmentInfoByDepartmentCode(String departmentCode) {
        return this.departmentInfoMapper.deleteByDepartmentCode(departmentCode);
    }

    @Override
    public List<DepartmentInfo> getDepartmentInfoByDepartmentIdList(List<Integer> departmentIdList) {
        if (departmentIdList == null || departmentIdList.isEmpty()) {
            return List.of();
        }
        return this.departmentInfoMapper.selectByDepartmentIdList(departmentIdList);
    }

    @Override
    public Integer deleteBatchByDepartmentIdList(List<Integer> departmentIdList) {
        if (departmentIdList == null || departmentIdList.isEmpty()) {
            return 0;
        }
        return this.departmentInfoMapper.deleteBatchByDepartmentIdList(departmentIdList);
    }

    @Override
    public Integer updateSortOrderBatch(List<DepartmentInfo> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return this.departmentInfoMapper.updateSortOrderBatch(list);
    }
}
