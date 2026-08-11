package com.easymall.service.impl;

import com.easymall.constants.Constants;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.po.SysProductProperty;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.query.SysProductPropertyQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.mappers.SysProductPropertyMapper;
import com.easymall.service.SysProductPropertyService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 业务接口实现
 */
@Service("sysProductPropertyService")
public class SysProductPropertyServiceImpl implements SysProductPropertyService {

    @Resource
    private SysProductPropertyMapper<SysProductProperty, SysProductPropertyQuery> sysProductPropertyMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<SysProductProperty> findListByParam(SysProductPropertyQuery param) {
        return this.sysProductPropertyMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(SysProductPropertyQuery param) {
        return this.sysProductPropertyMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<SysProductProperty> findListByPage(SysProductPropertyQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<SysProductProperty> list = this.findListByParam(param);
        PaginationResultVO<SysProductProperty> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(SysProductProperty bean) {
        return this.sysProductPropertyMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<SysProductProperty> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysProductPropertyMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<SysProductProperty> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysProductPropertyMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(SysProductProperty bean, SysProductPropertyQuery param) {
        StringTools.checkParam(param);
        return this.sysProductPropertyMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(SysProductPropertyQuery param) {
        StringTools.checkParam(param);
        return this.sysProductPropertyMapper.deleteByParam(param);
    }

    /**
     * 根据PropertyId获取对象
     */
    @Override
    public SysProductProperty getSysProductPropertyByPropertyId(String propertyId) {
        return this.sysProductPropertyMapper.selectByPropertyId(propertyId);
    }

    /**
     * 根据PropertyId修改
     */
    @Override
    public Integer updateSysProductPropertyByPropertyId(SysProductProperty bean, String propertyId) {
        return this.sysProductPropertyMapper.updateByPropertyId(bean, propertyId);
    }

    /**
     * 根据PropertyId删除
     */
    @Override
    public Integer deleteSysProductPropertyByPropertyId(String propertyId) {
        return this.sysProductPropertyMapper.deleteByPropertyId(propertyId);
    }

    @Override
    public void saveSysProductPropertyService(SysProductProperty productProperty) {
        if (StringTools.isEmpty(productProperty.getPropertyId())) {
            productProperty.setPropertyId(StringTools.getRandomNumber(Constants.LENGTH_5));
            productProperty.setPropertySort(this.sysProductPropertyMapper.selectMaxSort(productProperty.getCategoryId()) + 1);
            this.sysProductPropertyMapper.insert(productProperty);
        } else {
            this.sysProductPropertyMapper.updateByPropertyId(productProperty, productProperty.getPropertyId());
        }
    }

    @Override
    public void deleteSysProductPropertyService(String propertyId) {
        sysProductPropertyMapper.deleteByPropertyId(propertyId);
    }
}