package com.nexora.entity.query;

import java.util.Date;


/**
 * 用户表参数
 */
public class UserInfoQuery extends BaseParam {


	/**
	 * 用户ID
	 */
	private String userId;

	/**
	 * 登录名
	 */
	private String username;

	private String usernameFuzzy;

	/**
	 * 邮箱，登录核心字段，可空（管理员可不填）
	 */
	private String email;

	private String emailFuzzy;

	/**
	 * 密码（MD5存储）
	 */
	private String password;

	private String passwordFuzzy;

	/**
	 * 昵称
	 */
	private String nickName;

	private String nickNameFuzzy;

	/**
	 * 头像URL
	 */
	private String avatar;

	private String avatarFuzzy;

	/**
	 * 角色：0管理员 1学生
	 */
	private Integer roleType;

	/**
	 * 学段：PRIMARY_LOW/PRIMARY_HIGH/JUNIOR/SENIOR；学生必填，管理员为空
	 */
	private String stage;

	private String stageFuzzy;

	/**
	 * 年级（如三年级）
	 */
	private String grade;

	private String gradeFuzzy;

	/**
	 * 兴趣标签，JSON数组字符串
	 */
	private String interests;

	private String interestsFuzzy;

	/**
	 * 学习风格标签JSON【冗余：规则引擎按行为周期计算后落地】
	 */
	private String learningStyleTags;

	private String learningStyleTagsFuzzy;

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
	private String lastLoginTime;

	private String lastLoginTimeStart;

	private String lastLoginTimeEnd;

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


	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return this.username;
	}

	public void setUsernameFuzzy(String usernameFuzzy){
		this.usernameFuzzy = usernameFuzzy;
	}

	public String getUsernameFuzzy(){
		return this.usernameFuzzy;
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

	public void setNickName(String nickName){
		this.nickName = nickName;
	}

	public String getNickName(){
		return this.nickName;
	}

	public void setNickNameFuzzy(String nickNameFuzzy){
		this.nickNameFuzzy = nickNameFuzzy;
	}

	public String getNickNameFuzzy(){
		return this.nickNameFuzzy;
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

	public void setStage(String stage){
		this.stage = stage;
	}

	public String getStage(){
		return this.stage;
	}

	public void setStageFuzzy(String stageFuzzy){
		this.stageFuzzy = stageFuzzy;
	}

	public String getStageFuzzy(){
		return this.stageFuzzy;
	}

	public void setGrade(String grade){
		this.grade = grade;
	}

	public String getGrade(){
		return this.grade;
	}

	public void setGradeFuzzy(String gradeFuzzy){
		this.gradeFuzzy = gradeFuzzy;
	}

	public String getGradeFuzzy(){
		return this.gradeFuzzy;
	}

	public void setInterests(String interests){
		this.interests = interests;
	}

	public String getInterests(){
		return this.interests;
	}

	public void setInterestsFuzzy(String interestsFuzzy){
		this.interestsFuzzy = interestsFuzzy;
	}

	public String getInterestsFuzzy(){
		return this.interestsFuzzy;
	}

	public void setLearningStyleTags(String learningStyleTags){
		this.learningStyleTags = learningStyleTags;
	}

	public String getLearningStyleTags(){
		return this.learningStyleTags;
	}

	public void setLearningStyleTagsFuzzy(String learningStyleTagsFuzzy){
		this.learningStyleTagsFuzzy = learningStyleTagsFuzzy;
	}

	public String getLearningStyleTagsFuzzy(){
		return this.learningStyleTagsFuzzy;
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
