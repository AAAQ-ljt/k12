package com.easymall.mappers;

import org.apache.ibatis.annotations.Param;

/**
 *  数据库操作接口
 */
public interface UserAddressMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据AddressId更新
	 */
	 Integer updateByAddressId(@Param("bean") T t,@Param("addressId") String addressId);


	/**
	 * 根据AddressId删除
	 */
	 Integer deleteByAddressId(@Param("addressId") String addressId);


	/**
	 * 根据AddressId获取对象
	 */
	 T selectByAddressId(@Param("addressId") String addressId);


}
