package com.smart.campus.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.utils.DateUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户表
 */
public class UserInfo implements Serializable {

	public interface CreateStudent {}

	public interface UpdateStudent extends CreateStudent {}

	public interface CreateTeacher {}

	public interface UpdateTeacher extends CreateTeacher {}


	/**
	 * 主键ID
	 */
	@NotNull(message = "用户ID不能为空", groups = {UpdateStudent.class, UpdateTeacher.class})
	private Integer userId;

	/**
	 * 用户编号/学号/工号/管理员账号
	 */
	@NotBlank(message = "用户编号不能为空", groups = {CreateStudent.class, UpdateStudent.class, CreateTeacher.class, UpdateTeacher.class})
	private String userNo;

	/**
	 * 登录密码
	 */
	@JsonIgnore
	private String password;

	/**
	 * 真实姓名
	 */
	@NotBlank(message = "姓名不能为空", groups = {CreateStudent.class, UpdateStudent.class, CreateTeacher.class, UpdateTeacher.class})
	private String realName;

	/**
	 * 性别: 1男 2女 0未知
	 */
	@NotNull(message = "性别不能为空", groups = {CreateStudent.class, UpdateStudent.class, CreateTeacher.class, UpdateTeacher.class})
	private Integer gender;

	/**
	 * 手机号
	 */
	@NotBlank(message = "手机号不能为空", groups = {CreateStudent.class, UpdateStudent.class, CreateTeacher.class, UpdateTeacher.class})
	private String phone;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 头像地址
	 */
	private String avatar;

	/**
	 * 角色类型: 0:管理员 1:老师  2:学生
	 */
	private Integer roleType;

	/**
	 * 班级ID
	 */
	@NotBlank(message = "班级不能为空", groups = {CreateStudent.class, UpdateStudent.class, CreateTeacher.class, UpdateTeacher.class})
	private String classId;

	/**
	 * 职称(教师用)
	 */
	@NotBlank(message = "职称不能为空", groups = {CreateTeacher.class, UpdateTeacher.class})
	private String titleName;

	/**
	 * 状态: 1启用 0停用
	 */
	@NotNull(message = "状态不能为空", groups = {CreateStudent.class, UpdateStudent.class, CreateTeacher.class, UpdateTeacher.class})
	private Integer status;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;


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

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setRealName(String realName){
		this.realName = realName;
	}

	public String getRealName(){
		return this.realName;
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

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setAvatar(String avatar){
		this.avatar = avatar;
	}

	public String getAvatar(){
		return this.avatar;
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

	public void setTitleName(String titleName){
		this.titleName = titleName;
	}

	public String getTitleName(){
		return this.titleName;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setLastLoginTime(Date lastLoginTime){
		this.lastLoginTime = lastLoginTime;
	}

	public Date getLastLoginTime(){
		return this.lastLoginTime;
	}

	@Override
	public String toString (){
		return "主键ID:"+(userId == null ? "空" : userId)+"，用户编号/学号/工号/管理员账号:"+(userNo == null ? "空" : userNo)+"，登录密码:"+(password == null ? "空" : password)+"，真实姓名:"+(realName == null ? "空" : realName)+"，性别: 1男 2女 0未知:"+(gender == null ? "空" : gender)+"，手机号:"+(phone == null ? "空" : phone)+"，邮箱:"+(email == null ? "空" : email)+"，头像地址:"+(avatar == null ? "空" : avatar)+"，角色类型: 0:管理员 1:老师  2:学生:"+(roleType == null ? "空" : roleType)+"，班级ID:"+(classId == null ? "空" : classId)+"，职称(教师用):"+(titleName == null ? "空" : titleName)+"，状态: 1启用 0停用:"+(status == null ? "空" : status)+"，最后登录时间:"+(lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
