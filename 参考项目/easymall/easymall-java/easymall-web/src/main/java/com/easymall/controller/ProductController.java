package com.easymall.controller;

import com.easymall.component.EsSearchComponent;
import com.easymall.entity.enums.CommendTypeEnum;
import com.easymall.entity.enums.ProductStatusEnum;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.ProductInfoService;
import com.easymall.service.SysCategoryService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/product")
@Validated
public class ProductController extends ABaseController {


    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private SysCategoryService sysCategoryService;

    @Resource
    private EsSearchComponent esSearchComponent;

    @RequestMapping("/loadCategory")
    public ResponseVO loadCategory() {
        return getSuccessResponseVO(sysCategoryService.getAllCategoryList());
    }

    @RequestMapping("/loadCommendProduct")
    public ResponseVO loadCommendProduct() {
        ProductInfoQuery productInfoQuery = new ProductInfoQuery();
        productInfoQuery.setOrderBy("create_time desc");
        productInfoQuery.setStatus(ProductStatusEnum.ON_SALE.getStatus());
        productInfoQuery.setCommendType(CommendTypeEnum.COMMEND.getType());
        productInfoQuery.setSimplePage(new SimplePage(0, 11));
        List<ProductInfo> productInfoList = productInfoService.findListByParam(productInfoQuery);
        return getSuccessResponseVO(productInfoList);
    }

    @RequestMapping("/loadProduct")
    public ResponseVO loadProductList(Integer pageNo, String categoryId) {
        ProductInfoQuery productInfoQuery = new ProductInfoQuery();
        productInfoQuery.setPageNo(pageNo);
        productInfoQuery.setOrderBy("create_time desc");
        productInfoQuery.setCategoryIdOrPCategoryId(categoryId);
        productInfoQuery.setStatus(ProductStatusEnum.ON_SALE.getStatus());
        if (StringTools.isEmpty(categoryId)) {
            productInfoQuery.setCommendType(CommendTypeEnum.NOT_COMMEND.getType());
        }
        PaginationResultVO resultVO = productInfoService.findListByPage(productInfoQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/getProduct")
    public ResponseVO getProduct(@NotEmpty String productId) {
        return getSuccessResponseVO(productInfoService.getProductInfo(productId));
    }

    @RequestMapping("/search")
    public ResponseVO search(@NotEmpty String keyWords, BigDecimal priceFrom, BigDecimal priceTo, String sortType, String sortField, @Min(1) Integer pageNo) {
        return getSuccessResponseVO(esSearchComponent.searchProducts(keyWords, priceFrom, priceTo, sortType, sortField, pageNo));
    }
}
