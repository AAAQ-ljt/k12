package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单权限关系表 数据库操作接口
 */
public interface SystemRoleMenuMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据Id更新
     */
    Integer updateById(@Param("bean") T t, @Param("id") Integer id);

    /**
     * 根据Id删除
     */
    Integer deleteById(@Param("id") Integer id);

    /**
     * 根据Id获取对象
     */
    T selectById(@Param("id") Integer id);
}
