package com.smart.campus.admin.biz;

import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.query.MajorInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.DepartmentInfoService;
import com.smart.campus.service.MajorInfoService;
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
public class MajorAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final String ORDER_BY_ASC = "m.sort_order asc,m.major_id desc";
    private static final String ORDER_BY_DESC = "m.sort_order desc,m.major_id desc";

    @Resource
    private MajorInfoService majorInfoService;

    @Resource
    private DepartmentInfoService departmentInfoService;

    @Resource
    private ClassInfoService classInfoService;

    public PaginationResultVO<MajorInfo> loadDataList(MajorInfoQuery query) {
        return majorInfoService.findListByPage(buildPageQuery(query));
    }

    public List<MajorInfo> loadSortList(MajorInfoQuery query) {
        return majorInfoService.findListByParam(buildSortQuery(query));
    }

    public MajorInfo getMajorInfoById(Integer majorId) {
        MajorInfo majorInfo = majorInfoService.getMajorInfoByMajorId(majorId);
        if (majorInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "专业信息不存在");
        }
        return majorInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public MajorInfo add(MajorInfo bean) {
        fillDefaultValue(bean, null);
        checkDepartmentExists(bean.getDepartmentId());
        checkMajorCode(bean.getMajorCode(), null);
        majorInfoService.add(bean);
        return bean;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMajorInfoById(MajorInfo bean) {
        Integer majorId = bean.getMajorId();
        MajorInfo original = majorInfoService.getMajorInfoByMajorId(majorId);
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "专业信息不存在");
        }
        fillDefaultValue(bean, original.getSortOrder());
        checkDepartmentExists(bean.getDepartmentId());
        checkMajorCode(bean.getMajorCode(), majorId);
        majorInfoService.updateMajorInfoByMajorId(bean, majorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMajorInfoById(Integer majorId) {
        checkDeletePermission(majorId);
        majorInfoService.deleteMajorInfoByMajorId(majorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(String ids) {
        List<Integer> idList = StringTools.convertIds2List(ids);
        if (idList.size() < 2) {
            return;
        }

        List<MajorInfo> records = majorInfoService.getMajorInfoByMajorIdList(idList);
        Map<Integer, MajorInfo> recordMap = records.stream()
                .collect(Collectors.toMap(MajorInfo::getMajorId, item -> item));
        for (Integer majorId : idList) {
            if (!recordMap.containsKey(majorId)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的专业记录，无法更新排序");
            }
        }

        List<Integer> sortValues = buildSortValues(records);
        List<MajorInfo> updateList = new ArrayList<>();
        for (int index = 0; index < idList.size(); index++) {
            MajorInfo updateBean = new MajorInfo();
            updateBean.setMajorId(idList.get(index));
            updateBean.setSortOrder(sortValues.get(index));
            updateList.add(updateBean);
        }
        majorInfoService.updateSortOrderBatch(updateList);
    }

    private MajorInfoQuery buildPageQuery(MajorInfoQuery query) {
        MajorInfoQuery request = query == null ? new MajorInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(ORDER_BY_ASC);
        return request;
    }

    private MajorInfoQuery buildSortQuery(MajorInfoQuery query) {
        MajorInfoQuery request = query == null ? new MajorInfoQuery() : query;
        request.setSimplePage(null);
        request.setPageNo(null);
        request.setPageSize(null);
        request.setOrderBy(ORDER_BY_ASC);
        return request;
    }

    private void fillDefaultValue(MajorInfo bean, Integer currentSortOrder) {
        bean.setMajorCode(StringTools.trim(bean.getMajorCode()));
        bean.setMajorName(StringTools.trim(bean.getMajorName()));
        bean.setDescription(StringTools.trim(bean.getDescription()));
        if (bean.getStatus() == null) {
            bean.setStatus(StatusEnum.ENABLED.getCode());
        }
        if (bean.getSortOrder() == null) {
            bean.setSortOrder(currentSortOrder != null ? currentSortOrder : getNextSortOrder());
        }
    }

    private int getNextSortOrder() {
        MajorInfoQuery query = new MajorInfoQuery();
        query.setOrderBy(ORDER_BY_DESC);
        query.setSimplePage(new SimplePage(0, 1));
        List<MajorInfo> list = majorInfoService.findListByParam(query);
        if (list == null || list.isEmpty() || list.get(0).getSortOrder() == null) {
            return 1;
        }
        return list.get(0).getSortOrder() + 1;
    }

    private List<Integer> buildSortValues(List<MajorInfo> records) {
        List<MajorInfo> currentOrder = new ArrayList<>(records);
        currentOrder.sort(
                Comparator.comparing(MajorInfo::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(MajorInfo::getMajorId, Comparator.reverseOrder())
        );

        int maxSortOrder = getNextSortOrder() - 1;
        Set<Integer> usedSortOrder = new HashSet<>();
        List<Integer> sortValues = new ArrayList<>();
        for (MajorInfo item : currentOrder) {
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

    private void checkDepartmentExists(Integer departmentId) {
        DepartmentInfo departmentInfo = departmentInfoService.getDepartmentInfoByDepartmentId(departmentId);
        if (departmentInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "所属院系不存在");
        }
    }

    private void checkMajorCode(String majorCode, Integer currentMajorId) {
        MajorInfo savedMajor = majorInfoService.getMajorInfoByMajorCode(StringTools.trim(majorCode));
        if (savedMajor == null) {
            return;
        }
        if (currentMajorId != null && currentMajorId.equals(savedMajor.getMajorId())) {
            return;
        }
        throw new BusinessException(ResponseCodeEnum.CODE_601.getCode(), "专业编码已存在");
    }

    private void checkDeletePermission(Integer majorId) {
        MajorInfo majorInfo = majorInfoService.getMajorInfoByMajorId(majorId);
        if (majorInfo == null) {
            return;
        }
        ClassInfoQuery classInfoQuery = new ClassInfoQuery();
        classInfoQuery.setMajorId(majorId);
        Integer classCount = classInfoService.findCountByParam(classInfoQuery);
        if (classCount != null && classCount > 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "专业“" + majorInfo.getMajorName() + "”下仍存在班级，无法删除");
        }
    }
}
