package com.smart.campus.service.impl;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.query.MajorInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.mappers.MajorInfoMapper;
import com.smart.campus.service.MajorInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专业表 业务接口实现
 */
@Service("majorInfoService")
public class MajorInfoServiceImpl implements MajorInfoService {

    @Resource
    private MajorInfoMapper<MajorInfo, MajorInfoQuery> majorInfoMapper;

    @Override
    public List<MajorInfo> findListByParam(MajorInfoQuery param) {
        return this.majorInfoMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(MajorInfoQuery param) {
        return this.majorInfoMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<MajorInfo> findListByPage(MajorInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MajorInfo> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(MajorInfo bean) {
        return this.majorInfoMapper.insert(bean);
    }

    @Override
    public Integer addBatch(List<MajorInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.majorInfoMapper.insertBatch(listBean);
    }

    @Override
    public Integer addOrUpdateBatch(List<MajorInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.majorInfoMapper.insertOrUpdateBatch(listBean);
    }

    @Override
    public Integer updateByParam(MajorInfo bean, MajorInfoQuery param) {
        StringTools.checkParam(param);
        return this.majorInfoMapper.updateByParam(bean, param);
    }

    @Override
    public Integer deleteByParam(MajorInfoQuery param) {
        StringTools.checkParam(param);
        return this.majorInfoMapper.deleteByParam(param);
    }

    @Override
    public MajorInfo getMajorInfoByMajorId(Integer majorId) {
        return this.majorInfoMapper.selectByMajorId(majorId);
    }

    @Override
    public Integer updateMajorInfoByMajorId(MajorInfo bean, Integer majorId) {
        return this.majorInfoMapper.updateByMajorId(bean, majorId);
    }

    @Override
    public Integer deleteMajorInfoByMajorId(Integer majorId) {
        return this.majorInfoMapper.deleteByMajorId(majorId);
    }

    @Override
    public MajorInfo getMajorInfoByMajorCode(String majorCode) {
        return this.majorInfoMapper.selectByMajorCode(majorCode);
    }

    @Override
    public Integer updateMajorInfoByMajorCode(MajorInfo bean, String majorCode) {
        return this.majorInfoMapper.updateByMajorCode(bean, majorCode);
    }

    @Override
    public Integer deleteMajorInfoByMajorCode(String majorCode) {
        return this.majorInfoMapper.deleteByMajorCode(majorCode);
    }

    @Override
    public List<MajorInfo> getMajorInfoByMajorIdList(List<Integer> majorIdList) {
        if (majorIdList == null || majorIdList.isEmpty()) {
            return List.of();
        }
        return this.majorInfoMapper.selectByMajorIdList(majorIdList);
    }

    @Override
    public Integer updateSortOrderBatch(List<MajorInfo> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return this.majorInfoMapper.updateSortOrderBatch(list);
    }
}
