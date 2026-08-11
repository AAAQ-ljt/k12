package com.smart.campus.admin.biz;

import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.MajorInfoService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClassAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final String ORDER_BY_ASC = "c.sort_order asc,c.class_id desc";
    private static final String ORDER_BY_DESC = "c.sort_order desc,c.class_id desc";

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private MajorInfoService majorInfoService;

    @Resource
    private UserInfoService userInfoService;

    public PaginationResultVO<ClassInfo> loadDataList(ClassInfoQuery query) {
        return classInfoService.findListByPage(buildPageQuery(query));
    }

    public List<ClassInfo> loadSortList(ClassInfoQuery query) {
        return classInfoService.findListByParam(buildSortQuery(query));
    }

    public ClassInfo getClassInfoById(Integer classId) {
        ClassInfo classInfo = classInfoService.getClassInfoByClassId(classId);
        if (classInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "班级信息不存在");
        }
        return classInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public ClassInfo add(ClassInfo bean) {
        fillDefaultValue(bean, null);
        MajorInfo majorInfo = checkMajorExists(bean.getMajorId());
        bean.setDepartmentId(majorInfo.getDepartmentId());
        classInfoService.add(bean);
        return bean;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateClassInfoById(ClassInfo bean) {
        Integer classId = bean.getClassId();
        ClassInfo original = classInfoService.getClassInfoByClassId(classId);
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "班级信息不存在");
        }
        fillDefaultValue(bean, original.getSortOrder());
        MajorInfo majorInfo = checkMajorExists(bean.getMajorId());
        bean.setDepartmentId(majorInfo.getDepartmentId());
        classInfoService.updateClassInfoByClassId(bean, classId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteClassInfoById(Integer classId) {
        checkDeletePermission(List.of(classId));
        classInfoService.deleteClassInfoByClassId(classId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        List<Integer> idList = StringTools.convertIds2List(ids);
        checkDeletePermission(idList);
        classInfoService.deleteBatchByClassIdList(idList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(String ids) {
        List<Integer> idList = StringTools.convertIds2List(ids);
        if (idList.size() < 2) {
            return;
        }

        List<ClassInfo> records = classInfoService.getClassInfoByClassIdList(idList);
        Map<Integer, ClassInfo> recordMap = records.stream()
                .collect(Collectors.toMap(ClassInfo::getClassId, item -> item));
        for (Integer classId : idList) {
            if (!recordMap.containsKey(classId)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的班级记录，无法更新排序");
            }
        }

        List<Integer> sortValues = buildSortValues(records);
        List<ClassInfo> updateList = new ArrayList<>();
        for (int index = 0; index < idList.size(); index++) {
            ClassInfo updateBean = new ClassInfo();
            updateBean.setClassId(idList.get(index));
            updateBean.setSortOrder(sortValues.get(index));
            updateList.add(updateBean);
        }
        classInfoService.updateSortOrderBatch(updateList);
    }

    private ClassInfoQuery buildPageQuery(ClassInfoQuery query) {
        ClassInfoQuery request = query == null ? new ClassInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(ORDER_BY_ASC);
        return request;
    }

    private ClassInfoQuery buildSortQuery(ClassInfoQuery query) {
        ClassInfoQuery request = query == null ? new ClassInfoQuery() : query;
        request.setSimplePage(null);
        request.setPageNo(null);
        request.setPageSize(null);
        request.setOrderBy(ORDER_BY_ASC);
        return request;
    }

    private void fillDefaultValue(ClassInfo bean, Integer currentSortOrder) {
        bean.setClassName(StringTools.trim(bean.getClassName()));
        bean.setCounselorName(StringTools.trim(bean.getCounselorName()));
        bean.setHeadTeacherName(StringTools.trim(bean.getHeadTeacherName()));
        bean.setDescription(StringTools.trim(bean.getDescription()));
        if (bean.getStatus() == null) {
            bean.setStatus(StatusEnum.ENABLED.getCode());
        }
        if (bean.getSortOrder() == null) {
            bean.setSortOrder(currentSortOrder != null ? currentSortOrder : getNextSortOrder());
        }
    }

    private MajorInfo checkMajorExists(Integer majorId) {
        MajorInfo majorInfo = majorInfoService.getMajorInfoByMajorId(majorId);
        if (majorInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "所属专业不存在");
        }
        return majorInfo;
    }

    private int getNextSortOrder() {
        ClassInfoQuery query = new ClassInfoQuery();
        query.setOrderBy(ORDER_BY_DESC);
        query.setSimplePage(new SimplePage(0, 1));
        List<ClassInfo> list = classInfoService.findListByParam(query);
        if (list == null || list.isEmpty() || list.get(0).getSortOrder() == null) {
            return 1;
        }
        return list.get(0).getSortOrder() + 1;
    }

    private List<Integer> buildSortValues(List<ClassInfo> records) {
        List<ClassInfo> currentOrder = new ArrayList<>(records);
        currentOrder.sort(
                Comparator.comparing(ClassInfo::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ClassInfo::getClassId, Comparator.reverseOrder())
        );

        int maxSortOrder = getNextSortOrder() - 1;
        Set<Integer> usedSortOrder = new HashSet<>();
        List<Integer> sortValues = new ArrayList<>();
        for (ClassInfo item : currentOrder) {
            Integer sortOrder = item.getSortOrder();
            if (sortOrder == null || !usedSortOrder.add(sortOrder)) {
                sortOrder = ++maxSortOrder;
                usedSortOrder.add(sortOrder);
            }
            sortValues.add(sortOrder);
        }
        sortValues.sort(Integer::compareTo);
        return sortValues;
    }

    private void checkDeletePermission(List<Integer> classIdList) {
        List<ClassInfo> classList = classInfoService.getClassInfoByClassIdList(classIdList);
        Map<Integer, String> classNameMap = classList.stream()
                .collect(Collectors.toMap(ClassInfo::getClassId, ClassInfo::getClassName));
        List<Integer> usedClassIdList = userInfoService.getUsedClassIdList(classIdList);
        if (usedClassIdList.isEmpty()) {
            return;
        }
        List<String> usedClassNames = usedClassIdList.stream()
                .map(classNameMap::get)
                .filter(name -> name != null && !name.isEmpty())
                .toList();
        if (!usedClassNames.isEmpty()) {
            throw new BusinessException(
                    ResponseCodeEnum.CODE_600.getCode(),
                    "班级“" + String.join("、", usedClassNames) + "”已被师生数据引用，无法删除"
            );
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前班级已被师生数据引用，无法删除");
    }
}
