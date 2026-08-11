package com.easymall.service.impl;

import com.easymall.component.RedisComponent;
import com.easymall.constants.Constants;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.po.SysCategory;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.query.SysCategoryQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.mappers.SysCategoryMapper;
import com.easymall.service.SysCategoryService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * 业务接口实现
 */
@Service("sysCategoryService")
public class SysCategoryServiceImpl implements SysCategoryService {

    @Resource
    private SysCategoryMapper<SysCategory, SysCategoryQuery> sysCategoryMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<SysCategory> findListByParam(SysCategoryQuery param) {
        List<SysCategory> sysCategoryList = this.sysCategoryMapper.selectList(param);
        if (param.getConvert2Tree()) {
            sysCategoryList = convertLine2Tree(sysCategoryList, Constants.ZERO_STR);
        }
        return sysCategoryList;
    }

    private List<SysCategory> convertLine2Tree(List<SysCategory> dataList, String pid) {
        List<SysCategory> children = new ArrayList();
        for (SysCategory m : dataList) {
            if (m.getCategoryId() != null && m.getpCategoryId() != null && m.getpCategoryId().equals(pid)) {
                m.setChildren(convertLine2Tree(dataList, m.getCategoryId()));
                children.add(m);
            }
        }
        return children;
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(SysCategoryQuery param) {
        return this.sysCategoryMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<SysCategory> findListByPage(SysCategoryQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<SysCategory> list = this.findListByParam(param);
        PaginationResultVO<SysCategory> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(SysCategory bean) {
        return this.sysCategoryMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<SysCategory> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysCategoryMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<SysCategory> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysCategoryMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(SysCategory bean, SysCategoryQuery param) {
        StringTools.checkParam(param);
        return this.sysCategoryMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(SysCategoryQuery param) {
        StringTools.checkParam(param);
        return this.sysCategoryMapper.deleteByParam(param);
    }

    /**
     * 根据CategoryId获取对象
     */
    @Override
    public SysCategory getSysCategoryByCategoryId(String categoryId) {
        return this.sysCategoryMapper.selectByCategoryId(categoryId);
    }

    /**
     * 根据CategoryId修改
     */
    @Override
    public Integer updateSysCategoryByCategoryId(SysCategory bean, String categoryId) {
        return this.sysCategoryMapper.updateByCategoryId(bean, categoryId);
    }

    /**
     * 根据CategoryId删除
     */
    @Override
    public Integer deleteSysCategoryByCategoryId(String categoryId) {
        return this.sysCategoryMapper.deleteByCategoryId(categoryId);
    }


    @Override
    public void saveCategoryInfo(SysCategory bean) {
        if (bean.getCategoryId() == null) {
            bean.setCategoryId(StringTools.getRandomNumber(5));
            Integer maxSort = this.sysCategoryMapper.selectMaxSort(bean.getpCategoryId());
            bean.setSort(maxSort + 1);
            this.sysCategoryMapper.insert(bean);
        } else {
            this.sysCategoryMapper.updateByCategoryId(bean, bean.getCategoryId());
        }
        //刷新缓存
        save2Redis();
    }

    @Override
    public void delCategory(String categoryId) {
        SysCategoryQuery categoryInfoQuery = new SysCategoryQuery();
        categoryInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        sysCategoryMapper.deleteByParam(categoryInfoQuery);
        //刷新缓存
        save2Redis();
    }


    @Override
    public void changeSort(String categoryIds) {
        String[] categoryIdArray = categoryIds.split(",");
        List<SysCategory> categoryInfoList = new ArrayList<>();
        Integer sort = 1;
        for (String categoryId : categoryIdArray) {
            SysCategory categoryInfo = new SysCategory();
            categoryInfo.setCategoryId(categoryId);
            categoryInfo.setSort(sort++);
            categoryInfoList.add(categoryInfo);
        }
        this.sysCategoryMapper.updateSortBatch(categoryInfoList);
        save2Redis();
    }

    private void save2Redis() {
        SysCategoryQuery categoryInfoQuery = new SysCategoryQuery();
        categoryInfoQuery.setOrderBy("sort asc");
        List<SysCategory> categoryInfoList = findListByParam(categoryInfoQuery);
        redisComponent.saveCategoryList(categoryInfoList);
    }

    @Override
    public List<SysCategory> getAllCategoryList() {
        List<SysCategory> categoryInfoList = redisComponent.getCategoryList();
        if (categoryInfoList == null || categoryInfoList.isEmpty()) {
            save2Redis();
        }
        categoryInfoList = redisComponent.getCategoryList();
        return redisComponent.getCategoryList();
    }
}