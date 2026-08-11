package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.ClassInfoMapper;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("classInfoService")
public class ClassInfoServiceImpl implements ClassInfoService {

    @Resource
    private ClassInfoMapper<ClassInfo, ClassInfoQuery> classInfoMapper;

    @Override
    public List<ClassInfo> findListByParam(ClassInfoQuery param) {
        return this.classInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(ClassInfoQuery param) {
        return this.classInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<ClassInfo> findListByPage(ClassInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ClassInfo> list = this.findListByParam(param);
        return new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(ClassInfo bean) {
        return this.classInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<ClassInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.classInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<ClassInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.classInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(ClassInfo bean, ClassInfoQuery param) {
        StringTools.checkParam(param);
        return this.classInfoMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(ClassInfoQuery param) {
        StringTools.checkParam(param);
        return this.classInfoMapper.deleteByParam(param);
    }

    @Override
    public ClassInfo getClassInfoByClassId(Integer classId) {
        return this.classInfoMapper.selectByClassId(classId);
    }

    @Override
    public Integer updateClassInfoByClassId(ClassInfo bean, Integer classId) {
        return this.classInfoMapper.updateByClassId(bean, classId);
    }

    @Override
    public Integer deleteClassInfoByClassId(Integer classId) {
        return this.classInfoMapper.deleteByClassId(classId);
    }

    @Override
    public List<ClassInfo> getClassInfoByClassIdList(List<Integer> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            return List.of();
        }
        return this.classInfoMapper.selectByClassIdList(classIdList);
    }

    @Override
    public Integer deleteBatchByClassIdList(List<Integer> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            return 0;
        }
        return this.classInfoMapper.deleteBatchByClassIdList(classIdList);
    }

    @Override
    public Integer updateSortOrderBatch(List<ClassInfo> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return this.classInfoMapper.updateSortOrderBatch(list);
    }
}
