package com.nexora.entity.query;

import java.util.Date;


/**
 * 角色菜单关联表参数
 */
public class SystemRoleMenuQuery extends BaseParam {


	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 角色：0管理员（学生端无菜单权限，不进此表）
	 */
	private Integer roleType;

	/**
	 * 菜单ID
	 */
	private Integer menuId;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;


	public void setId(Integer id){
		this.id = id;
	}

	public Integer getId(){
		return this.id;
	}

	public void setRoleType(Integer roleType){
		this.roleType = roleType;
	}

	public Integer getRoleType(){
		return this.roleType;
	}

	public void setMenuId(Integer menuId){
		this.menuId = menuId;
	}

	public Integer getMenuId(){
		return this.menuId;
	}

	public void setCreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getCreateTime(){
		return this.createTime;
	}

	public void setCreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}
	public void setCreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

}
