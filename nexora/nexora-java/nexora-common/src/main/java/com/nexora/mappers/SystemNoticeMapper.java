package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 系统通知表 数据库操作接口
 */
public interface SystemNoticeMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据NoticeId更新
	 */
	 Integer updateByNoticeId(@Param("bean") T t,@Param("noticeId") Integer noticeId);


	/**
	 * 根据NoticeId删除
	 */
	 Integer deleteByNoticeId(@Param("noticeId") Integer noticeId);


	/**
	 * 根据NoticeId获取对象
	 */
	 T selectByNoticeId(@Param("noticeId") Integer noticeId);


}
