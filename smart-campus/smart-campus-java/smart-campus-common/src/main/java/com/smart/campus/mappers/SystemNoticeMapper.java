package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 系统通知公告表 数据库操作接口
 */
public interface SystemNoticeMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据NoticeId更新
	 */
	 Integer updateByNoticeId(@Param("bean") T t,@Param("noticeId") String noticeId);


	/**
	 * 根据NoticeId删除
	 */
	 Integer deleteByNoticeId(@Param("noticeId") String noticeId);


	/**
	 * 根据NoticeId获取对象
	 */
	 T selectByNoticeId(@Param("noticeId") String noticeId);


}
