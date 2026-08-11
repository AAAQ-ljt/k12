package com.easymall.service.impl;

import com.easymall.constants.Constants;
import com.easymall.entity.enums.DefaultTypeEnum;
import com.easymall.entity.enums.PageSize;
import com.easymall.entity.po.UserAddress;
import com.easymall.entity.query.SimplePage;
import com.easymall.entity.query.UserAddressQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.mappers.UserAddressMapper;
import com.easymall.service.UserAddressService;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 业务接口实现
 */
@Service("userAddressService")
public class UserAddressServiceImpl implements UserAddressService {

    @Resource
    private UserAddressMapper<UserAddress, UserAddressQuery> userAddressMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserAddress> findListByParam(UserAddressQuery param) {
        return this.userAddressMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserAddressQuery param) {
        return this.userAddressMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserAddress> findListByPage(UserAddressQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserAddress> list = this.findListByParam(param);
        PaginationResultVO<UserAddress> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserAddress bean) {
        return this.userAddressMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserAddress> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userAddressMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserAddress> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userAddressMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(UserAddress bean, UserAddressQuery param) {
        StringTools.checkParam(param);
        return this.userAddressMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(UserAddressQuery param) {
        StringTools.checkParam(param);
        return this.userAddressMapper.deleteByParam(param);
    }

    /**
     * 根据AddressId获取对象
     */
    @Override
    public UserAddress getUserAddressByAddressId(String addressId) {
        return this.userAddressMapper.selectByAddressId(addressId);
    }

    /**
     * 根据AddressId修改
     */
    @Override
    public Integer updateUserAddressByAddressId(UserAddress bean, String addressId) {
        return this.userAddressMapper.updateByAddressId(bean, addressId);
    }

    /**
     * 根据AddressId删除
     */
    @Override
    public Integer deleteUserAddressByAddressId(String addressId) {
        return this.userAddressMapper.deleteByAddressId(addressId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDefaultAddress(String addressId, String userId) {

        restDefault(userId);

        UserAddress userAddress = new UserAddress();
        userAddress.setDefaultType(DefaultTypeEnum.DEFAULT.getType());

        UserAddressQuery userAddressQuery = new UserAddressQuery();
        userAddressQuery.setAddressId(addressId);
        userAddressQuery.setUserId(userId);
        this.userAddressMapper.updateByParam(userAddress, userAddressQuery);
    }

    private void restDefault(String userId) {
        UserAddress updateAddress = new UserAddress();
        updateAddress.setDefaultType(DefaultTypeEnum.NOT_DEFAULT.getType());

        UserAddressQuery query = new UserAddressQuery();
        query.setUserId(userId);
        this.userAddressMapper.updateByParam(updateAddress, query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAdderss(UserAddress userAddress) {
        if (DefaultTypeEnum.DEFAULT.getType().equals(userAddress.getDefaultType())) {
            restDefault(userAddress.getUserId());
        }
        if (StringTools.isEmpty(userAddress.getAddressId())) {
            userAddress.setAddressId(StringTools.getRandomString(Constants.LENGTH_15));
            this.userAddressMapper.insert(userAddress);
        } else {
            UserAddressQuery userAddressQuery = new UserAddressQuery();
            userAddressQuery.setAddressId(userAddress.getAddressId());
            userAddressQuery.setUserId(userAddress.getUserId());
            this.userAddressMapper.updateByParam(userAddress, userAddressQuery);
        }

    }
}