package com.smart.campus.web.biz;

import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.SystemNotice;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.query.SystemNoticeQuery;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.SystemNoticeService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import com.smart.campus.web.entity.vo.notice.SystemNoticeDetailVO;
import com.smart.campus.web.entity.vo.notice.SystemNoticeListItemVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SystemNoticeWebBiz {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int STATUS_PUBLISHED = 1;
    private static final int TARGET_TYPE_STUDENT = 1;
    private static final int TARGET_TYPE_CLASS = 2;
    private static final int TARGET_TYPE_MAJOR = 3;
    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_LATEST_SIZE = 5;
    private static final String NOTICE_ORDER_BY = "s.is_top desc,s.publish_time desc,s.create_time desc";

    @Resource
    private SystemNoticeService systemNoticeService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ClassInfoService classInfoService;

    public PaginationResultVO<SystemNoticeListItemVO> loadLatest(Integer pageNo, Integer pageSize) {
        UserInfo currentStudent = getCurrentStudent();
        SystemNoticeQuery query = new SystemNoticeQuery();
        query.setStatus(STATUS_PUBLISHED);
        query.setOrderBy(NOTICE_ORDER_BY);
        int currentPageNo = pageNo == null || pageNo <= 0 ? DEFAULT_PAGE_NO : pageNo;
        int currentPageSize = pageSize == null || pageSize <= 0 ? DEFAULT_LATEST_SIZE : Math.min(pageSize, 20);
        List<SystemNoticeListItemVO> matchedList = new ArrayList<>();
        for (SystemNotice notice : systemNoticeService.findListByParam(query)) {
            if (canStudentReadNotice(currentStudent, notice)) {
                matchedList.add(buildListItem(notice));
            }
        }
        int totalCount = matchedList.size();
        int pageTotal = totalCount == 0 ? 0 : (totalCount + currentPageSize - 1) / currentPageSize;
        int start = Math.min((currentPageNo - 1) * currentPageSize, totalCount);
        int end = Math.min(start + currentPageSize, totalCount);
        return new PaginationResultVO<>(totalCount, currentPageSize, currentPageNo, pageTotal, matchedList.subList(start, end));
    }

    @Transactional(rollbackFor = Exception.class)
    public SystemNoticeDetailVO getDetail(String noticeId) {
        UserInfo currentStudent = getCurrentStudent();
        SystemNotice notice = systemNoticeService.getSystemNoticeByNoticeId(StringTools.trim(noticeId));
        if (notice == null || !Objects.equals(notice.getStatus(), STATUS_PUBLISHED) || !canStudentReadNotice(currentStudent, notice)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "公告不存在或无权查看");
        }
        SystemNotice update = new SystemNotice();
        update.setViewCount((notice.getViewCount() == null ? 0 : notice.getViewCount()) + 1);
        systemNoticeService.updateSystemNoticeByNoticeId(update, notice.getNoticeId());
        notice.setViewCount(update.getViewCount());
        return buildDetail(notice);
    }

    private boolean canStudentReadNotice(UserInfo currentStudent, SystemNotice notice) {
        Integer targetType = notice.getTargetType();
        if (Objects.equals(targetType, TARGET_TYPE_STUDENT)) {
            return true;
        }
        if (Objects.equals(targetType, TARGET_TYPE_CLASS)) {
            return hasAnyClass(currentStudent.getClassId(), parseTargetIntegerIds(notice.getTargetIds()));
        }
        if (Objects.equals(targetType, TARGET_TYPE_MAJOR)) {
            return hasAnyClass(currentStudent.getClassId(), loadMajorClassIdList(parseTargetIntegerIds(notice.getTargetIds())));
        }
        return false;
    }

    private boolean hasAnyClass(String classIdText, List<Integer> classIdList) {
        if (StringTools.isEmpty(classIdText) || classIdList == null || classIdList.isEmpty()) {
            return false;
        }
        Set<Integer> classIdSet = new LinkedHashSet<>(classIdList);
        for (String item : classIdText.split(",")) {
            String value = StringTools.trim(item);
            if (StringTools.isEmpty(value)) {
                continue;
            }
            try {
                if (classIdSet.contains(Integer.valueOf(value))) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private List<Integer> loadMajorClassIdList(List<Integer> majorIdList) {
        if (majorIdList == null || majorIdList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> classIdSet = new LinkedHashSet<>();
        for (Integer majorId : new LinkedHashSet<>(majorIdList)) {
            ClassInfoQuery query = new ClassInfoQuery();
            query.setMajorId(majorId);
            classInfoService.findListByParam(query).stream()
                    .map(ClassInfo::getClassId)
                    .filter(Objects::nonNull)
                    .forEach(classIdSet::add);
        }
        return new ArrayList<>(classIdSet);
    }

    private List<Integer> parseTargetIntegerIds(String targetIds) {
        if (StringTools.isEmpty(targetIds)) {
            return List.of();
        }
        LinkedHashSet<Integer> idSet = new LinkedHashSet<>();
        for (String targetId : targetIds.split(",")) {
            if (StringTools.isEmpty(targetId)) {
                continue;
            }
            try {
                idSet.add(Integer.valueOf(StringTools.trim(targetId)));
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<>(idSet);
    }

    private SystemNoticeListItemVO buildListItem(SystemNotice notice) {
        SystemNoticeListItemVO vo = new SystemNoticeListItemVO();
        vo.setNoticeId(notice.getNoticeId());
        vo.setNoticeTitle(notice.getNoticeTitle());
        vo.setIsTop(notice.getIsTop());
        vo.setPublishTime(notice.getPublishTime());
        vo.setViewCount(notice.getViewCount());
        return vo;
    }

    private SystemNoticeDetailVO buildDetail(SystemNotice notice) {
        SystemNoticeDetailVO vo = new SystemNoticeDetailVO();
        vo.setNoticeId(notice.getNoticeId());
        vo.setNoticeTitle(notice.getNoticeTitle());
        vo.setNoticeContent(notice.getNoticeContent());
        vo.setIsTop(notice.getIsTop());
        vo.setPublishTime(notice.getPublishTime());
        vo.setViewCount(notice.getViewCount());
        vo.setCreateUserName(notice.getCreateUserName());
        return vo;
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号查看公告");
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生信息不存在");
        }
        return userInfo;
    }
}
