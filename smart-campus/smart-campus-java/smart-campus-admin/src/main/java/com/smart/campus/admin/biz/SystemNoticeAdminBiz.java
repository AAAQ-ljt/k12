package com.smart.campus.admin.biz;

import com.smart.campus.admin.biz.support.MessagePublishAdminSupport;
import com.smart.campus.admin.entity.dto.SystemNoticeSaveDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.po.MessageInfo;
import com.smart.campus.entity.po.SystemNotice;
import com.smart.campus.entity.query.MessageInfoQuery;
import com.smart.campus.entity.query.SystemNoticeQuery;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.admin.entity.vo.SystemNoticeDetailVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.MajorInfoService;
import com.smart.campus.service.MessageInfoService;
import com.smart.campus.service.SystemNoticeService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SystemNoticeAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int TARGET_TYPE_STUDENT = 1;
    private static final int TARGET_TYPE_CLASS = 2;
    private static final int TARGET_TYPE_MAJOR = 3;
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_OFFLINE = 2;
    private static final int IS_TOP_NO = 0;
    private static final int BIZ_TYPE_NOTICE = 4;
    private static final String NOTICE_ORDER_BY = "s.is_top desc,s.publish_time desc,s.create_time desc";

    @Resource
    private SystemNoticeService systemNoticeService;

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private MajorInfoService majorInfoService;

    @Resource
    private MessageInfoService messageInfoService;

    @Resource
    private MessagePublishAdminSupport messagePublishAdminSupport;

    public PaginationResultVO<SystemNotice> loadDataList(SystemNoticeQuery query) {
        SystemNoticeQuery request = query == null ? new SystemNoticeQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(NOTICE_ORDER_BY);
        return systemNoticeService.findListByPage(request);
    }

    public SystemNoticeDetailVO getSystemNoticeById(String noticeId) {
        SystemNotice notice = getNoticeOrThrow(StringTools.trim(noticeId));
        return buildDetail(notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public SystemNoticeDetailVO add(SystemNoticeSaveDTO dto) {
        SystemNoticeSaveDTO request = normalizeSaveDTO(dto);
        validateSaveDTO(request);
        LoginUserVO loginUser = getCurrentLoginUser();
        String noticeId = generateStringId();
        SystemNotice notice = buildNotice(request, noticeId, loginUser, null);
        systemNoticeService.add(notice);
        return getSystemNoticeById(noticeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSystemNoticeById(SystemNoticeSaveDTO dto) {
        SystemNoticeSaveDTO request = normalizeSaveDTO(dto);
        SystemNotice original = getNoticeOrThrow(request.getNoticeId());
        validateSaveDTO(request);
        SystemNotice notice = buildNotice(request, original.getNoticeId(), getCurrentLoginUser(), original);
        systemNoticeService.updateSystemNoticeByNoticeId(notice, original.getNoticeId());
        syncNoticeMessage(original.getNoticeId(), notice);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(String noticeId) {
        String normalizedNoticeId = StringTools.trim(noticeId);
        SystemNotice notice = getNoticeOrThrow(normalizedNoticeId);
        if ((Objects.equals(notice.getTargetType(), TARGET_TYPE_CLASS) || Objects.equals(notice.getTargetType(), TARGET_TYPE_MAJOR)) && StringTools.isEmpty(notice.getTargetIds())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请先选择公告发布范围");
        }
        if (!Objects.equals(notice.getStatus(), STATUS_PUBLISHED)) {
            SystemNotice update = new SystemNotice();
            update.setStatus(STATUS_PUBLISHED);
            update.setPublishTime(new Date());
            systemNoticeService.updateSystemNoticeByNoticeId(update, normalizedNoticeId);
            notice = systemNoticeService.getSystemNoticeByNoticeId(normalizedNoticeId);
        }
        if (!hasNoticeMessage(normalizedNoticeId)) {
            messagePublishAdminSupport.sendNoticePublishMessage(notice, getCurrentLoginUser());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void offline(String noticeId) {
        SystemNotice notice = getNoticeOrThrow(StringTools.trim(noticeId));
        if (Objects.equals(notice.getStatus(), STATUS_OFFLINE)) {
            return;
        }
        SystemNotice update = new SystemNotice();
        update.setStatus(STATUS_OFFLINE);
        systemNoticeService.updateSystemNoticeByNoticeId(update, notice.getNoticeId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSystemNoticeById(String noticeId) {
        deleteNotice(StringTools.trim(noticeId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        for (String noticeId : parseStringIds(ids)) {
            deleteNotice(noticeId);
        }
    }

    private void deleteNotice(String noticeId) {
        SystemNotice notice = systemNoticeService.getSystemNoticeByNoticeId(noticeId);
        if (notice == null) {
            return;
        }
        if (Objects.equals(notice.getStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "已发布公告请先下线后再删除");
        }
        systemNoticeService.deleteSystemNoticeByNoticeId(noticeId);
    }

    private SystemNoticeSaveDTO normalizeSaveDTO(SystemNoticeSaveDTO dto) {
        SystemNoticeSaveDTO request = dto == null ? new SystemNoticeSaveDTO() : dto;
        request.setNoticeId(StringTools.trim(request.getNoticeId()));
        request.setNoticeTitle(StringTools.trim(request.getNoticeTitle()));
        request.setNoticeContent(StringTools.trim(request.getNoticeContent()));
        request.setTargetIds(StringTools.trim(request.getTargetIds()));
        request.setTargetIdList(normalizeTargetIdList(request));
        if (request.getIsTop() == null) {
            request.setIsTop(IS_TOP_NO);
        }
        return request;
    }

    private void validateSaveDTO(SystemNoticeSaveDTO dto) {
        if (StringTools.isEmpty(dto.getNoticeTitle())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "公告标题不能为空");
        }
        if (StringTools.isEmpty(dto.getNoticeContent())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "公告内容不能为空");
        }
        Integer targetType = dto.getTargetType();
        if (!Objects.equals(targetType, TARGET_TYPE_STUDENT)
                && !Objects.equals(targetType, TARGET_TYPE_CLASS)
                && !Objects.equals(targetType, TARGET_TYPE_MAJOR)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "发布范围不正确");
        }
        if (Objects.equals(targetType, TARGET_TYPE_CLASS)) {
            validateClassTargets(dto.getTargetIdList());
        }
        if (Objects.equals(targetType, TARGET_TYPE_MAJOR)) {
            validateMajorTargets(dto.getTargetIdList());
        }
    }

    private void validateClassTargets(List<String> targetIdList) {
        if (targetIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请选择发布班级");
        }
        List<Integer> classIdList = parseIntegerIds(targetIdList, "发布班级不正确");
        List<ClassInfo> classList = classInfoService.getClassInfoByClassIdList(classIdList);
        if (classList.size() != classIdList.size()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的班级信息");
        }
    }

    private void validateMajorTargets(List<String> targetIdList) {
        if (targetIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请选择发布专业");
        }
        List<Integer> majorIdList = parseIntegerIds(targetIdList, "发布专业不正确");
        List<MajorInfo> majorList = majorInfoService.getMajorInfoByMajorIdList(majorIdList);
        if (majorList.size() != majorIdList.size()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的专业信息");
        }
    }

    private SystemNotice buildNotice(SystemNoticeSaveDTO dto, String noticeId, LoginUserVO loginUser, SystemNotice original) {
        SystemNotice notice = new SystemNotice();
        notice.setNoticeId(noticeId);
        notice.setNoticeTitle(dto.getNoticeTitle());
        notice.setNoticeContent(dto.getNoticeContent());
        notice.setTargetType(dto.getTargetType());
        notice.setTargetIds(buildTargetIds(dto));
        notice.setIsTop(dto.getIsTop());
        notice.setStatus(original == null ? STATUS_DRAFT : original.getStatus());
        notice.setViewCount(original == null ? 0 : original.getViewCount());
        if (original == null) {
            notice.setCreateUserId(loginUser.getUserId());
            notice.setCreateUserName(resolveUserName(loginUser));
            notice.setCreateTime(new Date());
        }
        return notice;
    }

    private String buildTargetIds(SystemNoticeSaveDTO dto) {
        if (!Objects.equals(dto.getTargetType(), TARGET_TYPE_CLASS) && !Objects.equals(dto.getTargetType(), TARGET_TYPE_MAJOR)) {
            return null;
        }
        return String.join(",", dto.getTargetIdList());
    }

    private void syncNoticeMessage(String noticeId, SystemNotice notice) {
        MessageInfoQuery query = new MessageInfoQuery();
        query.setBizType(BIZ_TYPE_NOTICE);
        query.setBizId(noticeId);
        List<MessageInfo> messageList = messageInfoService.findListByParam(query);
        if (messageList == null || messageList.isEmpty()) {
            return;
        }
        for (MessageInfo item : messageList) {
            MessageInfo update = new MessageInfo();
            update.setMessageTitle(notice.getNoticeTitle());
            update.setMessageContent(notice.getNoticeContent());
            messageInfoService.updateMessageInfoByMessageId(update, item.getMessageId());
        }
    }

    private boolean hasNoticeMessage(String noticeId) {
        MessageInfoQuery query = new MessageInfoQuery();
        query.setBizType(BIZ_TYPE_NOTICE);
        query.setBizId(noticeId);
        return messageInfoService.findCountByParam(query) > 0;
    }

    private SystemNoticeDetailVO buildDetail(SystemNotice notice) {
        SystemNoticeDetailVO vo = new SystemNoticeDetailVO();
        vo.setNoticeId(notice.getNoticeId());
        vo.setNoticeTitle(notice.getNoticeTitle());
        vo.setNoticeContent(notice.getNoticeContent());
        vo.setTargetType(notice.getTargetType());
        vo.setTargetTypeText(resolveTargetTypeText(notice.getTargetType()));
        vo.setStatus(notice.getStatus());
        vo.setStatusText(resolveStatusText(notice.getStatus()));
        vo.setIsTop(notice.getIsTop());
        vo.setPublishTime(notice.getPublishTime());
        vo.setViewCount(notice.getViewCount());
        vo.setCreateUserId(notice.getCreateUserId());
        vo.setCreateUserName(notice.getCreateUserName());
        vo.setCreateTime(notice.getCreateTime());
        vo.setLastUpdateTime(notice.getLastUpdateTime());
        vo.setTargetIds(notice.getTargetIds());
        vo.setTargetIdList(parseTargetIdList(notice.getTargetIds()));
        return vo;
    }

    private List<String> parseTargetIdList(String targetIds) {
        if (StringTools.isEmpty(targetIds)) {
            return List.of();
        }
        return List.of(targetIds.split(",")).stream()
                .map(StringTools::trim)
                .filter(item -> !StringTools.isEmpty(item))
                .distinct()
                .toList();
    }

    private SystemNotice getNoticeOrThrow(String noticeId) {
        SystemNotice notice = systemNoticeService.getSystemNoticeByNoticeId(noticeId);
        if (notice == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "公告信息不存在");
        }
        return notice;
    }

    private LoginUserVO getCurrentLoginUser() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        return loginUser;
    }

    private List<String> normalizeTargetIdList(SystemNoticeSaveDTO dto) {
        LinkedHashSet<String> targetIdSet = new LinkedHashSet<>();
        if (dto.getTargetIdList() != null) {
            dto.getTargetIdList().stream()
                    .map(StringTools::trim)
                    .filter(item -> !StringTools.isEmpty(item))
                    .forEach(targetIdSet::add);
        }
        if (!StringTools.isEmpty(dto.getTargetIds())) {
            List.of(dto.getTargetIds().split(",")).stream()
                    .map(StringTools::trim)
                    .filter(item -> !StringTools.isEmpty(item))
                    .forEach(targetIdSet::add);
        }
        return new ArrayList<>(targetIdSet);
    }

    private List<Integer> parseIntegerIds(List<String> idList, String message) {
        List<Integer> result = new ArrayList<>();
        for (String item : idList) {
            try {
                result.add(Integer.valueOf(item));
            } catch (NumberFormatException e) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), message);
            }
        }
        return result;
    }

    private List<String> parseStringIds(String ids) {
        if (StringTools.isEmpty(ids)) {
            return List.of();
        }
        return List.of(ids.split(",")).stream()
                .map(StringTools::trim)
                .filter(item -> !StringTools.isEmpty(item))
                .distinct()
                .toList();
    }

    private String resolveUserName(LoginUserVO loginUser) {
        String realName = loginUser == null ? null : StringTools.trim(loginUser.getRealName());
        return StringTools.isEmpty(realName) ? "管理员" : realName;
    }

    private String resolveTargetTypeText(Integer targetType) {
        if (Objects.equals(targetType, TARGET_TYPE_STUDENT)) {
            return "全部学生";
        }
        if (Objects.equals(targetType, TARGET_TYPE_CLASS)) {
            return "指定班级";
        }
        if (Objects.equals(targetType, TARGET_TYPE_MAJOR)) {
            return "指定专业";
        }
        return "未知范围";
    }

    private String resolveStatusText(Integer status) {
        if (Objects.equals(status, STATUS_PUBLISHED)) {
            return "已发布";
        }
        if (Objects.equals(status, STATUS_OFFLINE)) {
            return "已下线";
        }
        return "草稿";
    }

    private String generateStringId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
