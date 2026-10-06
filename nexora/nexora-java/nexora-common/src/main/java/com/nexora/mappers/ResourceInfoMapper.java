package com.nexora.mappers;

import com.nexora.entity.po.ResourceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源信息表 数据库操作接口
 */
public interface ResourceInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ResourceId更新
	 */
	 Integer updateByResourceId(@Param("bean") T t,@Param("resourceId") String resourceId);


	/**
	 * 根据ResourceId删除
	 */
	 Integer deleteByResourceId(@Param("resourceId") String resourceId);


	/**
	 * 根据ResourceId获取对象
	 */
	 T selectByResourceId(@Param("resourceId") String resourceId);

	/**
	 * 批量更新所属目录
	 */
	 Integer updateDirectoryBatch(@Param("list") List<ResourceInfo> list);

	/**
	 * 统计指定用户已占用空间（处理中 + 可用）
	 */
	 Long selectUsedSizeByOwner(@Param("ownerId") String ownerId);

	/**
	 * 批量改状态（僵尸记录清理：把确认无进行中上传的「处理中」资源置为失败）
	 */
	 Integer updateStatusByIds(@Param("resourceIds") List<String> resourceIds,
							   @Param("status") Integer status,
							   @Param("updateTime") java.util.Date updateTime);

}
