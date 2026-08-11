package com.nexora.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import com.nexora.entity.enums.DateTimePatternEnum;
import com.nexora.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;


/**
 * 用户表
 */
public class UserInfo implements Serializable {


	/**
	 * 用户ID
	 */
	private Integer userId;

	/**
	 * 登录名
	 */
	private String username;

	/**
	 * 邮箱，登录核心字段，可空（管理员可不填）
	 */
	private String email;

	/**
	 * 密码（MD5存储）
	 */
	private String password;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 头像URL
	 */
	private String avatar;

	/**
	 * 角色：0管理员 1学生
	 */
	private Integer roleType;

	/**
	 * 学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR；学生必填，管理员为空
	 */
	private String stage;

	/**
	 * 年级（如三年级）
	 */
	private String grade;

	/**
	 * 兴趣标签，JSON数组字符串
	 */
	private String interests;

	/**
	 * 学习风格标签JSON【冗余：规则引擎按行为周期计算后落地】
	 */
	private String learningStyleTags;

	/**
	 * 性别：0女 1男 2保密
	 */
	private Integer sex;

	/**
	 * 状态：0禁用 1启用
	 */
	private Integer status;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

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


	public void setUserId(Integer userId){
		this.userId = userId;
	}

	public Integer getUserId(){
		return this.userId;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return this.username;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return this.password;
	}

	public void setNickName(String nickName){
		this.nickName = nickName;
	}

	public String getNickName(){
		return this.nickName;
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

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setGrade(String grade){
		this.grade = grade;
	}

	public String getGrade(){
		return this.grade;
	}

	public void setInterests(String interests){
		this.interests = interests;
	}

	public String getInterests(){
		return this.interests;
	}

	public void setLearningStyleTags(String learningStyleTags){
		this.learningStyleTags = learningStyleTags;
	}

	public String getLearningStyleTags(){
		return this.learningStyleTags;
	}

	public void setSex(Integer sex){
		this.sex = sex;
	}

	public Integer getSex(){
		return this.sex;
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
		return "用户ID:"+(userId == null ? "空" : userId)+"，登录名:"+(username == null ? "空" : username)+"，邮箱，登录核心字段，可空（管理员可不填）:"+(email == null ? "空" : email)+"，密码（MD5存储）:"+(password == null ? "空" : password)+"，昵称:"+(nickName == null ? "空" : nickName)+"，头像URL:"+(avatar == null ? "空" : avatar)+"，角色：0管理员 1学生:"+(roleType == null ? "空" : roleType)+"，学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR；学生必填，管理员为空:"+(stage == null ? "空" : stage)+"，年级（如三年级）:"+(grade == null ? "空" : grade)+"，兴趣标签，JSON数组字符串:"+(interests == null ? "空" : interests)+"，学习风格标签JSON【冗余：规则引擎按行为周期计算后落地】:"+(learningStyleTags == null ? "空" : learningStyleTags)+"，性别：0女 1男 2保密:"+(sex == null ? "空" : sex)+"，状态：0禁用 1启用:"+(status == null ? "空" : status)+"，最后登录时间:"+(lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，更新时间:"+(updateTime == null ? "空" : DateUtil.format(updateTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()));
	}
}
