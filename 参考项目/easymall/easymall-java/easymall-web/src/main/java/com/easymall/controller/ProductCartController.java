package com.easymall.controller;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.entity.po.ProductCart;
import com.easymall.entity.query.ProductCartQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.ProductCartService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 购物车 Controller
 */
@RestController("productCartController")
@RequestMapping("/productCart")
public class ProductCartController extends ABaseController {

    @Resource
    private ProductCartService productCartService;

    /**
     * 根据条件分页查询
     */
    @RequestMapping("/loadProductCart")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadProductCart(Integer pageNo) {
        ProductCartQuery query = new ProductCartQuery();
        query.setUserId(getTokenUserInfo().getUserId());
        query.setPageNo(pageNo);
        PaginationResultVO resultVO = productCartService.loadProductCart(query);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/add2Cart")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO add2Cart(ProductCart productCart) {
        productCart.setUserId(getTokenUserInfo().getUserId());
        productCartService.add2Cart(productCart);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteCart")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO deleteCart(@NotEmpty String cartId) {
        ProductCartQuery productCartQuery = new ProductCartQuery();
        productCartQuery.setCartId(cartId);
        productCartQuery.setUserId(getTokenUserInfo().getUserId());
        productCartService.deleteByParam(productCartQuery);
        return getSuccessResponseVO(null);
    }
}