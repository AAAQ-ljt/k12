package com.smart.campus.admin.biz;

import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.query.DepartmentInfoQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.DepartmentInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepartmentAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final String ORDER_BY_ASC = "d.sort_order asc,d.department_id desc";
    private static final String ORDER_BY_DESC = "d.sort_order desc,d.department_id desc";

    @Resource
    private DepartmentInfoService departmentInfoService;

    public PaginationResultVO<DepartmentInfo> loadDataList(DepartmentInfoQuery query) {
        DepartmentInfoQuery request = buildPageQuery(query);
        return departmentInfoService.findListByPage(request);
    }

    public List<DepartmentInfo> loadSortList(DepartmentInfoQuery query) {
        return departmentInfoService.findListByParam(buildSortQuery(query));
    }

    public DepartmentInfo getDepartmentInfoById(Integer departmentId) {
        DepartmentInfo departmentInfo = departmentInfoService.getDepartmentInfoByDepartmentId(departmentId);
        if (departmentInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "院系信息不存在");
        }
        return departmentInfo;
    }

    @Transactional(rollbackFor = Exception.class)
    public DepartmentInfo add(DepartmentInfo bean) {
        fillDefaultValue(bean, null);
        checkDepartmentCode(bean.getDepartmentCode(), null);
        departmentInfoService.add(bean);
        return bean;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDepartmentInfoById(DepartmentInfo bean) {
        Integer departmentId = bean.getDepartmentId();
        DepartmentInfo original = departmentInfoService.getDepartmentInfoByDepartmentId(departmentId);
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "院系信息不存在");
        }
        fillDefaultValue(bean, original.getSortOrder());
        checkDepartmentCode(bean.getDepartmentCode(), departmentId);
        departmentInfoService.updateDepartmentInfoByDepartmentId(bean, departmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartmentInfoById(Integer departmentId) {
        checkDeletePermission(departmentId);
        departmentInfoService.deleteDepartmentInfoByDepartmentId(departmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        List<Integer> idList = StringTools.convertIds2List(ids);
        List<DepartmentInfo> departmentList = departmentInfoService.getDepartmentInfoByDepartmentIdList(idList);
        List<String> usedDepartmentNames = departmentList.stream()
                .filter(item -> item.getMajorCount() != null && item.getMajorCount() > 0)
                .map(DepartmentInfo::getDepartmentName)
                .filter(Objects::nonNull)
                .toList();
        if (!usedDepartmentNames.isEmpty()) {
            throw new BusinessException(
                    ResponseCodeEnum.CODE_600.getCode(),
                    "院系“" + String.join("、", usedDepartmentNames) + "”下仍存在专业，无法删除"
            );
        }

        departmentInfoService.deleteBatchByDepartmentIdList(idList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(String ids) {
        List<Integer> idList = StringTools.convertIds2List(ids);
        if (idList.size() < 2) {
            return;
        }

        List<DepartmentInfo> records = departmentInfoService.getDepartmentInfoByDepartmentIdList(idList);
        Map<Integer, DepartmentInfo> recordMap = records.stream()
                .collect(Collectors.toMap(DepartmentInfo::getDepartmentId, item -> item));
        for (Integer departmentId : idList) {
            if (!recordMap.containsKey(departmentId)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的院系记录，无法更新排序");
            }
        }

        List<Integer> sortValues = buildSortValues(records);
        List<DepartmentInfo> updateList = new ArrayList<>();
        for (int index = 0; index < idList.size(); index++) {
            DepartmentInfo updateBean = new DepartmentInfo();
            updateBean.setDepartmentId(idList.get(index));
            updateBean.setSortOrder(sortValues.get(index));
            updateList.add(updateBean);
        }
        departmentInfoService.updateSortOrderBatch(updateList);
    }

    private DepartmentInfoQuery buildPageQuery(DepartmentInfoQuery query) {
        DepartmentInfoQuery request = query == null ? new DepartmentInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(ORDER_BY_ASC);
        return request;
    }

    private DepartmentInfoQuery buildSortQuery(DepartmentInfoQuery query) {
        DepartmentInfoQuery request = query == null ? new DepartmentInfoQuery() : query;
        request.setSimplePage(null);
        request.setPageNo(null);
        request.setPageSize(null);
        request.setOrderBy(ORDER_BY_ASC);
        return request;
    }

    private void fillDefaultValue(DepartmentInfo bean, Integer currentSortOrder) {
        bean.setDepartmentName(StringTools.trim(bean.getDepartmentName()));
        bean.setDepartmentCode(StringTools.trim(bean.getDepartmentCode()));
        bean.setLeaderName(StringTools.trim(bean.getLeaderName()));
        bean.setContactPhone(StringTools.trim(bean.getContactPhone()));
        bean.setDescription(StringTools.trim(bean.getDescription()));
        if (bean.getStatus() == null) {
            bean.setStatus(StatusEnum.ENABLED.getCode());
        }
        if (bean.getSortOrder() == null) {
            bean.setSortOrder(currentSortOrder != null ? currentSortOrder : getNextSortOrder());
        }
    }

    private int getNextSortOrder() {
        DepartmentInfoQuery query = new DepartmentInfoQuery();
        query.setOrderBy(ORDER_BY_DESC);
        query.setSimplePage(new SimplePage(0, 1));
        List<DepartmentInfo> list = departmentInfoService.findListByParam(query);
        if (list == null || list.isEmpty() || list.get(0).getSortOrder() == null) {
            return 1;
        }
        return list.get(0).getSortOrder() + 1;
    }

    private List<Integer> buildSortValues(List<DepartmentInfo> records) {
        List<DepartmentInfo> currentOrder = new ArrayList<>(records);
        currentOrder.sort(
                Comparator.comparing(DepartmentInfo::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(DepartmentInfo::getDepartmentId, Comparator.reverseOrder())
        );

        int maxSortOrder = getNextSortOrder() - 1;
        Set<Integer> usedSortOrder = new HashSet<>();
        List<Integer> sortValues = new ArrayList<>();
        for (DepartmentInfo item : currentOrder) {
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

    private void checkDepartmentCode(String departmentCode, Integer currentDepartmentId) {
        DepartmentInfo savedDepartment = departmentInfoService.getDepartmentInfoByDepartmentCode(StringTools.trim(departmentCode));
        if (savedDepartment == null) {
            return;
        }
        if (currentDepartmentId != null && currentDepartmentId.equals(savedDepartment.getDepartmentId())) {
            return;
        }
        throw new BusinessException(ResponseCodeEnum.CODE_601.getCode(), "院系编码已存在");
    }

    private void checkDeletePermission(Integer departmentId) {
        DepartmentInfo departmentInfo = departmentInfoService.getDepartmentInfoByDepartmentId(departmentId);
        if (departmentInfo == null) {
            return;
        }
        if (departmentInfo.getMajorCount() != null && departmentInfo.getMajorCount() > 0) {
            throw new BusinessException(
                    ResponseCodeEnum.CODE_600.getCode(),
                    "院系“" + departmentInfo.getDepartmentName() + "”下仍存在专业，无法删除"
            );
        }
    }
}
