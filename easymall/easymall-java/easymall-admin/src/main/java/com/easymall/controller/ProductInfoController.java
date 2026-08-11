package com.easymall.controller;

import com.easymall.entity.dto.ProductSaveDTO;
import com.easymall.entity.po.ProductInfo;
import com.easymall.entity.query.ProductInfoQuery;
import com.easymall.entity.valid.CreateGroup;
import com.easymall.entity.valid.UpdateGroup;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.ProductInfoService;
import com.easymall.service.ProductSkuService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 */
@RestController("productInfoController")
@RequestMapping("/productInfo")
@Validated
public class ProductInfoController extends ABaseController {

    @Resource
    private ProductInfoService productInfoService;

    @Resource
    private ProductSkuService productSkuService;

    @RequestMapping("/addProduct")
    public ResponseVO addProduct(@RequestBody @Validated(CreateGroup.class) ProductSaveDTO productSaveDTO) {
        productInfoService.saveProduct(productSaveDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateProduct")
    public ResponseVO updateProduct(@RequestBody @Validated(UpdateGroup.class) ProductSaveDTO productSaveDTO) {
        productInfoService.saveProduct(productSaveDTO);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getProductInfo")
    public ResponseVO getProductInfo(@NotEmpty String productId) {
        return getSuccessResponseVO(productInfoService.getProductInfo(productId));
    }

    @RequestMapping("/loadProduct")
    public ResponseVO loadProduct(String productNameFuzzy,
                                  Integer pageNo,
                                  Integer pageSize,
                                  String categoryIdOrPCategoryId,
                                  Integer status,
                                  Integer commendType) {
        ProductInfoQuery query = new ProductInfoQuery();
        query.setPageNo(pageNo);
        query.setStatus(status);
        query.setPageSize(pageSize);
        query.setCategoryIdOrPCategoryId(categoryIdOrPCategoryId);
        query.setProductNameFuzzy(productNameFuzzy);
        query.setCommendType(commendType);
        query.setOrderBy("p.create_time desc");
        PaginationResultVO resultVO = productInfoService.findListByPage4ListVO(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/updateSkuStock")
    public ResponseVO updateSkuStock(@NotEmpty String productId, @NotEmpty String propertyValueIdHash, @NotNull Integer changeStock) {
        productSkuService.updateStock(productId, propertyValueIdHash, changeStock);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateProductStatus")
    public ResponseVO updateProductStatus(@NotEmpty String productId, @NotNull Integer status) {
        productInfoService.updateStatus(productId, status);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/deleteProduct")
    public ResponseVO deleteProduct(@NotEmpty String productId) {
        productInfoService.deleteProduct(productId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/commendProduct")
    public ResponseVO commendProduct(@NotEmpty String productId,
                                     @NotNull Integer commendType) {
        ProductInfo updateInfo = new ProductInfo();
        updateInfo.setCommendType(commendType);
        productInfoService.updateProductInfoByProductId(updateInfo, productId);
        return getSuccessResponseVO(null);
    }
}