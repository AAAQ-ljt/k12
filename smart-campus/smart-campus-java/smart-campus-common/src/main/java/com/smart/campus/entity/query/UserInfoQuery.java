package com.smart.campus.entity.query;

import java.util.Date;


/**
 * 用户表参数
 */
public class UserInfoQuery extends BaseParam {


	/**
	 * 主键ID
	 */
	private Integer userId;

	/**
	 * 用户编号/学号/工号/管理员账号
	 */
	private String userNo;

	private String userNoFuzzy;

	/**
	 * 登录密码
	 */
	private String password;

	private String passwordFuzzy;

	/**
	 * 真实姓名
	 */
	private String realName;

	private String realNameFuzzy;

	/**
	 * 性别: 1男 2女 0未知
	 */
	private Integer gender;

	/**
	 * 手机号
	 */
	private String phone;

	private String phoneFuzzy;

	/**
	 * 邮箱
	 */
	private String email;

	private String emailFuzzy;

	/**
	 * 头像地址
	 */
	private String avatar;

	private String avatarFuzzy;

	/**
	 * 角色类型: 0:管理员 1:老师  2:学生
	 */
	private Integer roleType;

	/**
	 * 班级ID
	 */
	private String classId;

	private String classIdFuzzy;

	/**
	 * 职称(教师用)
	 */
	private String titleName;

	private String titleNameFuzzy;

	/**
	 * 状态: 1启用 0停用
	 */
	private Integer status;

	/**
	 * 最后登录时间
	 */
	private String lastLoginTime;

	private String lastLoginTimeStart;

	private String lastLoginTimeEnd;


	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setUserNo(String userNo){
		this.userNo = userNo;
	}

	public String getUserNo(){
		return this.userNo;
	}

	public void setUserNoFuzzy(String userNoFuzzy){
		this.userNoFuzzy = userNoFuzzy;
	}

	public String getUserNoFuzzy(){
		return this.userNoFuzzy;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setPasswordFuzzy(String passwordFuzzy){
		this.passwordFuzzy = passwordFuzzy;
	}

	public String getPasswordFuzzy(){
		return this.passwordFuzzy;
	}

	public void setRealName(String realName){
		this.realName = realName;
	}

	public String getRealName(){
		return this.realName;
	}

	public void setRealNameFuzzy(String realNameFuzzy){
		this.realNameFuzzy = realNameFuzzy;
	}

	public String getRealNameFuzzy(){
		return this.realNameFuzzy;
	}

	public void setGender(Integer gender){
		this.gender = gender;
	}

	public Integer getGender(){
		return this.gender;
	}

	public void setPhone(String phone){
		this.phone = phone;
	}

	public String getPhone(){
		return this.phone;
	}

	public void setPhoneFuzzy(String phoneFuzzy){
		this.phoneFuzzy = phoneFuzzy;
	}

	public String getPhoneFuzzy(){
		return this.phoneFuzzy;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setEmailFuzzy(String emailFuzzy){
		this.emailFuzzy = emailFuzzy;
	}

	public String getEmailFuzzy(){
		return this.emailFuzzy;
	}

	public void setAvatar(String avatar){
		this.avatar = avatar;
	}

	public String getAvatar(){
		return this.avatar;
	}

	public void setAvatarFuzzy(String avatarFuzzy){
		this.avatarFuzzy = avatarFuzzy;
	}

	public String getAvatarFuzzy(){
		return this.avatarFuzzy;
	}

	public void setRoleType(Integer roleType){
		this.roleType = roleType;
	}

	public Integer getRoleType(){
		return this.roleType;
	}

	public void setClassId(String classId){
		this.classId = classId;
	}

	public String getClassId(){
		return this.classId;
	}

	public void setClassIdFuzzy(String classIdFuzzy){
		this.classIdFuzzy = classIdFuzzy;
	}

	public String getClassIdFuzzy(){
		return this.classIdFuzzy;
	}

	public void setTitleName(String titleName){
		this.titleName = titleName;
	}

	public String getTitleName(){
		return this.titleName;
	}

	public void setTitleNameFuzzy(String titleNameFuzzy){
		this.titleNameFuzzy = titleNameFuzzy;
	}

	public String getTitleNameFuzzy(){
		return this.titleNameFuzzy;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setLastLoginTime(String lastLoginTime){
		this.lastLoginTime = lastLoginTime;
	}

	public String getLastLoginTime(){
		return this.lastLoginTime;
	}

	public void setLastLoginTimeStart(String lastLoginTimeStart){
		this.lastLoginTimeStart = lastLoginTimeStart;
	}

	public String getLastLoginTimeStart(){
		return this.lastLoginTimeStart;
	}
	public void setLastLoginTimeEnd(String lastLoginTimeEnd){
		this.lastLoginTimeEnd = lastLoginTimeEnd;
	}

	public String getLastLoginTimeEnd(){
		return this.lastLoginTimeEnd;
	}

}
