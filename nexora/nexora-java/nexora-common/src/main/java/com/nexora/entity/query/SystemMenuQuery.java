package com.nexora.entity.query;

import java.util.Date;


/**
 * 系统菜单表参数
 */
public class SystemMenuQuery extends BaseParam {


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

	private String menuNameFuzzy;

	/**
	 * 权限编码，与管理端权限注解一一对应
	 */
	private String menuCode;

	private String menuCodeFuzzy;

	/**
	 * 类型：0目录 1菜单 2按钮
	 */
	private Integer menuType;

	/**
	 * 前端路由路径
	 */
	private String path;

	private String pathFuzzy;

	/**
	 * 图标
	 */
	private String icon;

	private String iconFuzzy;

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
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 更新时间
	 */
	private String updateTime;

	private String updateTimeStart;

	private String updateTimeEnd;


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

	public void setMenuNameFuzzy(String menuNameFuzzy){
		this.menuNameFuzzy = menuNameFuzzy;
	}

	public String getMenuNameFuzzy(){
		return this.menuNameFuzzy;
	}

	public void setMenuCode(String menuCode){
		this.menuCode = menuCode;
	}

	public String getMenuCode(){
		return this.menuCode;
	}

	public void setMenuCodeFuzzy(String menuCodeFuzzy){
		this.menuCodeFuzzy = menuCodeFuzzy;
	}

	public String getMenuCodeFuzzy(){
		return this.menuCodeFuzzy;
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

	public void setPathFuzzy(String pathFuzzy){
		this.pathFuzzy = pathFuzzy;
	}

	public String getPathFuzzy(){
		return this.pathFuzzy;
	}

	public void setIcon(String icon){
		this.icon = icon;
	}

	public String getIcon(){
		return this.icon;
	}

	public void setIconFuzzy(String iconFuzzy){
		this.iconFuzzy = iconFuzzy;
	}

	public String getIconFuzzy(){
		return this.iconFuzzy;
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

	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}

	public String getUpdateTime(){
		return this.updateTime;
	}

	public void setUpdateTimeStart(String updateTimeStart){
		this.updateTimeStart = updateTimeStart;
	}

	public String getUpdateTimeStart(){
		return this.updateTimeStart;
	}
	public void setUpdateTimeEnd(String updateTimeEnd){
		this.updateTimeEnd = updateTimeEnd;
	}

	public String getUpdateTimeEnd(){
		return this.updateTimeEnd;
	}

}
