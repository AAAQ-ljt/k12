package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 系统菜单表
 */
public class SystemMenu implements Serializable {


	/**
	 * 菜单ID
	 */
	private Integer menuId;

	/**
	 * 父菜单ID，0为根
	 */
	private Integer parentId;

	/**
	 * 菜单名
	 */
	private String menuName;

	/**
	 * 权限编码，与管理端权限注解一一对应
	 */
	private String menuCode;

	/**
	 * 类型：0目录 1菜单 2按钮
	 */
	private Integer menuType;

	/**
	 * 前端路由路径
	 */
	private String path;

	/**
	 * 图标
	 */
	private String icon;

	/**
	 * 排序
	 */
	private Integer sort;

	/**
	 * 状态：0停用 1启用
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;


	public void setMenuId(Integer menuId){
		this.menuId = menuId;
	}

	public Integer getMenuId(){
		return this.menuId;
	}

	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public Integer getParentId(){
		return this.parentId;
	}

	public void setMenuName(String menuName){
		this.menuName = menuName;
	}

	public String getMenuName(){
		return this.menuName;
	}

	public void setMenuCode(String menuCode){
		this.menuCode = menuCode;
	}

	public String getMenuCode(){
		return this.menuCode;
	}

	public void setMenuType(Integer menuType){
		this.menuType = menuType;
	}

	public Integer getMenuType(){
		return this.menuType;
	}

	public void setPath(String path){
		this.path = path;
	}

	public String getPath(){
		return this.path;
	}

	public void setIcon(String icon){
		this.icon = icon;
	}

	public String getIcon(){
		return this.icon;
	}

	public void setSort(Integer sort){
		this.sort = sort;
	}

	public Integer getSort(){
		return this.sort;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setUpdateTime(Date updateTime){
		this.updateTime = updateTime;
	}

	public Date getUpdateTime(){
		return this.updateTime;
	}

	@Override
	public String toString (){
		return "菜单ID:"+(menuId == null ? "空" : menuId)+"，父菜单ID，0为根:"+(parentId == null ? "空" : parentId)+"，菜单名:"+(menuName == null ? "空" : menuName)+"，权限编码，与管理端权限注解一一对应:"+(menuCode == null ? "空" : menuCode)+"，类型：0目录 1菜单 2按钮:"+(menuType == null ? "空" : menuType)+"，前端路由路径:"+(path == null ? "空" : path)+"，图标:"+(icon == null ? "空" : icon)+"，排序:"+(sort == null ? "空" : sort)+"，状态：0停用 1启用:"+(status == null ? "空" : status)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
