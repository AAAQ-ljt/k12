package com.easymall.controller;

import com.easymall.entity.po.SysCategory;
import com.easymall.entity.po.SysProductProperty;
import com.easymall.entity.query.SysCategoryQuery;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.SysCategoryService;
import com.easymall.service.SysProductPropertyService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 */
@RestController("sysCategoryController")
@RequestMapping("/sysCategory")
public class SysCategoryController extends ABaseController {

    @Resource
    private SysCategoryService sysCategoryService;

    @Resource
    private SysProductPropertyService sysProductPropertyService;

    /**
     * 根据条件分页查询
     */
    @RequestMapping("/loadCategory")
    public ResponseVO loadCategory(Boolean queryProperty) {
        SysCategoryQuery sysCategoryQuery = new SysCategoryQuery();
        sysCategoryQuery.setOrderBy("s.sort asc");
        if (queryProperty != null && queryProperty) {
            sysCategoryQuery.setOrderBy("s.sort asc,sp.property_sort asc");
        }
        sysCategoryQuery.setQueryProperty(queryProperty);
        sysCategoryQuery.setConvert2Tree(true);
        return getSuccessResponseVO(sysCategoryService.findListByParam(sysCategoryQuery));
    }

    @RequestMapping("/saveCategory")
    public ResponseVO saveCategory(SysCategory category) {
        sysCategoryService.saveCategoryInfo(category);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delCategory")
    public ResponseVO delCategory(String categoryId) {
        sysCategoryService.delCategory(categoryId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeCategorySort")
    public ResponseVO changeCategorySort(String categoryIds) {
        sysCategoryService.changeSort(categoryIds);
        return getSuccessResponseVO(null);
    }

    /**
     * 保存商品属性
     *
     * @param productProperty
     * @return
     */
    @RequestMapping("/saveProductProperty")
    public ResponseVO saveProductProperty(SysProductProperty productProperty) {
        sysProductPropertyService.saveSysProductPropertyService(productProperty);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除商品属性
     *
     * @param propertyId
     * @return
     */
    @RequestMapping("/delProductProperty")
    public ResponseVO delProductProperty(String propertyId) {
        sysProductPropertyService.deleteSysProductPropertyByPropertyId(propertyId);
        return getSuccessResponseVO(null);
    }
}