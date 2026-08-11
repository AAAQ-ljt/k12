package com.smart.campus.admin.biz;

import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final String ORDER_BY_DESC = "u.user_id desc";
    private static final String DEFAULT_PASSWORD = "123456";

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ClassInfoService classInfoService;

    public PaginationResultVO<UserInfo> loadStudentList(UserInfoQuery query) {
        return userInfoService.findListByPage(buildPageQuery(query, UserRoleTypeEnum.STUDENT));
    }

    public PaginationResultVO<UserInfo> loadTeacherList(UserInfoQuery query) {
        return userInfoService.findListByPage(buildPageQuery(query, UserRoleTypeEnum.TEACHER));
    }

    public UserInfo getStudentById(Integer userId) {
        return getUserByRole(userId, UserRoleTypeEnum.STUDENT, "学生");
    }

    public UserInfo getTeacherById(Integer userId) {
        return getUserByRole(userId, UserRoleTypeEnum.TEACHER, "教师");
    }

    @Transactional(rollbackFor = Exception.class)
    public UserInfo addStudent(UserInfo bean) {
        prepareStudent(bean);
        checkUserNo(bean.getUserNo(), null);
        checkPhone(bean.getPhone(), null);
        bean.setPassword(encodePassword(DEFAULT_PASSWORD));
        userInfoService.add(bean);
        return bean;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStudentById(UserInfo bean) {
        UserInfo original = getUserByRole(bean.getUserId(), UserRoleTypeEnum.STUDENT, "学生");
        prepareStudent(bean);
        checkUserNo(bean.getUserNo(), original.getUserId());
        checkPhone(bean.getPhone(), original.getUserId());
        bean.setPassword(null);
        userInfoService.updateUserInfoByUserId(bean, original.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteStudentById(Integer userId) {
        getUserByRole(userId, UserRoleTypeEnum.STUDENT, "学生");
        userInfoService.deleteUserInfoByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteStudentBatch(String ids) {
        deleteBatch(ids, UserRoleTypeEnum.STUDENT, "学生");
    }

    @Transactional(rollbackFor = Exception.class)
    public UserInfo addTeacher(UserInfo bean) {
        prepareTeacher(bean);
        checkUserNo(bean.getUserNo(), null);
        checkPhone(bean.getPhone(), null);
        bean.setPassword(encodePassword(DEFAULT_PASSWORD));
        userInfoService.add(bean);
        return bean;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTeacherById(UserInfo bean) {
        UserInfo original = getUserByRole(bean.getUserId(), UserRoleTypeEnum.TEACHER, "教师");
        prepareTeacher(bean);
        checkUserNo(bean.getUserNo(), original.getUserId());
        checkPhone(bean.getPhone(), original.getUserId());
        bean.setPassword(null);
        userInfoService.updateUserInfoByUserId(bean, original.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTeacherById(Integer userId) {
        getUserByRole(userId, UserRoleTypeEnum.TEACHER, "教师");
        userInfoService.deleteUserInfoByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTeacherBatch(String ids) {
        deleteBatch(ids, UserRoleTypeEnum.TEACHER, "教师");
    }

    private UserInfoQuery buildPageQuery(UserInfoQuery query, UserRoleTypeEnum roleType) {
        UserInfoQuery request = query == null ? new UserInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setRoleType(roleType.getCode());
        request.setOrderBy(ORDER_BY_DESC);
        return request;
    }

    private UserInfo getUserByRole(Integer userId, UserRoleTypeEnum roleType, String roleName) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo == null || !roleType.getCode().equals(userInfo.getRoleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), roleName + "信息不存在");
        }
        return userInfo;
    }

    private void prepareStudent(UserInfo bean) {
        normalizeCommonFields(bean);
        bean.setRoleType(UserRoleTypeEnum.STUDENT.getCode());
        bean.setTitleName(null);
        bean.setClassId(normalizeSingleClassId(bean.getClassId()));
    }

    private void prepareTeacher(UserInfo bean) {
        normalizeCommonFields(bean);
        bean.setRoleType(UserRoleTypeEnum.TEACHER.getCode());
        bean.setTitleName(StringTools.trim(bean.getTitleName()));
        bean.setClassId(normalizeTeacherClassIds(bean.getClassId()));
    }

    private void normalizeCommonFields(UserInfo bean) {
        bean.setUserNo(StringTools.trim(bean.getUserNo()));
        bean.setRealName(StringTools.trim(bean.getRealName()));
        bean.setPhone(StringTools.trim(bean.getPhone()));
        bean.setEmail(StringTools.trim(bean.getEmail()));
        bean.setAvatar(StringTools.trim(bean.getAvatar()));
        if (bean.getStatus() == null) {
            bean.setStatus(StatusEnum.ENABLED.getCode());
        }
    }

    private String normalizeSingleClassId(String classIdValue) {
        List<Integer> classIdList = StringTools.convertIds2List(classIdValue);
        if (classIdList.size() != 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生必须绑定一个有效班级");
        }
        List<ClassInfo> classInfoList = classInfoService.getClassInfoByClassIdList(classIdList);
        if (classInfoList.size() != 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "所选班级不存在");
        }
        return String.valueOf(classIdList.get(0));
    }

    private String normalizeTeacherClassIds(String classIdValue) {
        List<Integer> classIdList = StringTools.convertIds2List(classIdValue);
        if (classIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请至少选择一个授课班级");
        }
        List<ClassInfo> classInfoList = classInfoService.getClassInfoByClassIdList(classIdList);
        Map<Integer, ClassInfo> classInfoMap = classInfoList.stream()
                .collect(Collectors.toMap(ClassInfo::getClassId, item -> item));
        for (Integer classId : classIdList) {
            if (!classInfoMap.containsKey(classId)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的授课班级");
            }
        }
        return classIdList.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private void checkUserNo(String userNo, Integer currentUserId) {
        UserInfo savedUser = userInfoService.getUserInfoByUserNo(userNo);
        if (savedUser == null) {
            return;
        }
        if (currentUserId != null && currentUserId.equals(savedUser.getUserId())) {
            return;
        }
        throw new BusinessException(ResponseCodeEnum.CODE_601.getCode(), "用户编号已存在");
    }

    private void checkPhone(String phone, Integer currentUserId) {
        UserInfo savedUser = userInfoService.getUserInfoByPhone(phone);
        if (savedUser == null) {
            return;
        }
        if (currentUserId != null && currentUserId.equals(savedUser.getUserId())) {
            return;
        }
        throw new BusinessException(ResponseCodeEnum.CODE_601.getCode(), "手机号已存在");
    }

    private void deleteBatch(String ids, UserRoleTypeEnum roleType, String roleName) {
        List<Integer> userIdList = StringTools.convertIds2List(ids);
        if (userIdList.isEmpty()) {
            return;
        }
        List<UserInfo> userList = userInfoService.getUserInfoByUserIdList(userIdList);
        Map<Integer, UserInfo> userMap = userList.stream()
                .collect(Collectors.toMap(UserInfo::getUserId, item -> item));
        for (Integer userId : userIdList) {
            UserInfo userInfo = userMap.get(userId);
            if (userInfo == null || !roleType.getCode().equals(userInfo.getRoleType())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的" + roleName + "记录");
            }
        }
        userInfoService.deleteBatchByUserIdList(userIdList);
    }

    private String encodePassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }
}
