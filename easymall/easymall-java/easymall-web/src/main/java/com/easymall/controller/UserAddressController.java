package com.easymall.controller;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.entity.dto.TokenUserInfoDTO;
import com.easymall.entity.po.UserAddress;
import com.easymall.entity.query.UserAddressQuery;
import com.easymall.entity.valid.CreateGroup;
import com.easymall.entity.valid.UpdateGroup;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.UserAddressService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 */
@RestController("userAddressController")
@RequestMapping("/userAddress")
@Validated
public class UserAddressController extends ABaseController {

    @Resource
    private UserAddressService userAddressService;

    /**
     * 根据条件分页查询
     */
    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadDataList() {
        UserAddressQuery query = new UserAddressQuery();
        query.setUserId(getTokenUserInfo().getUserId());
        query.setOrderBy("default_type desc");
        return getSuccessResponseVO(userAddressService.findListByParam(query));
    }

    @RequestMapping("/addAddress")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO addAddress(@Validated(CreateGroup.class) UserAddress userAddress) {
        userAddress.setUserId(getTokenUserInfo().getUserId());
        userAddressService.saveAdderss(userAddress);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateAddress")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateAddress(@Validated(UpdateGroup.class) UserAddress userAddress) {
        userAddress.setUserId(getTokenUserInfo().getUserId());
        userAddressService.saveAdderss(userAddress);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delAddress")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO delAddress(@NotEmpty String addressId) {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo();
        UserAddressQuery userAddressQuery = new UserAddressQuery();
        userAddressQuery.setUserId(tokenUserInfoDTO.getUserId());
        userAddressQuery.setAddressId(addressId);
        userAddressService.deleteByParam(userAddressQuery);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateDefault")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateDefault(@NotEmpty String addressId) {
        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfo();
        userAddressService.updateDefaultAddress(addressId, tokenUserInfoDTO.getUserId());
        return getSuccessResponseVO(null);
    }
}