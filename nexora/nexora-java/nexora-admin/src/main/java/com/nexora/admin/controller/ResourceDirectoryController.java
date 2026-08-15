package com.nexora.admin.controller;

import com.nexora.admin.dto.ResourceDirectorySortDTO;
import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceDirectoryQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceDirectoryService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 资源目录管理 Controller
 */
@RestController
@RequestMapping("/resourceDirectory")
public class ResourceDirectoryController extends ABaseController {

    @Resource
    private ResourceDirectoryService resourceDirectoryService;

    @Resource
    private ResourceInfoService resourceInfoService;

    /**
     * 获取目录树（返回扁平列表，前端组树）
     */
    @GetMapping("/getTree")
    public ResponseVO<List<ResourceDirectory>> getTree() {
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setOwnerIdNull(Boolean.TRUE);
        query.setOrderBy("parent_id asc, sort asc");
        return getSuccessResponseVO(resourceDirectoryService.findListByParam(query));
    }

    /**
     * 新建目录
     */
    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody ResourceDirectory bean) {
        if (StringTools.isEmpty(bean.getDirName())) {
            throw new BusinessException("目录名称不能为空");
        }
        if (StringTools.isEmpty(bean.getParentId()) || "root".equals(bean.getParentId())) {
            bean.setParentId("0");
        }
        ResourceDirectoryQuery query = new ResourceDirectoryQuery();
        query.setParentId(bean.getParentId());
        query.setOwnerIdNull(Boolean.TRUE);
        query.setOrderBy("sort desc");
        List<ResourceDirectory> list = resourceDirectoryService.findListByParam(query);
        int maxSort = list.isEmpty() || list.get(0).getSort() == null ? 0 : list.get(0).getSort();
        bean.setDirId(StringTools.getRandomNumber(Constants.LENGTH_15));
        bean.setSort(maxSort + 1);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        resourceDirectoryService.add(bean);
        return getSuccessResponseVO(bean.getDirId());
    }

    /**
     * 重命名目录
     */
    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody ResourceDirectory bean) {
        if (StringTools.isEmpty(bean.getDirId())) {
            throw new BusinessException("目录ID不能为空");
        }
        if (StringTools.isEmpty(bean.getDirName())) {
            throw new BusinessException("目录名称不能为空");
        }
        ResourceDirectory updateBean = new ResourceDirectory();
        updateBean.setDirName(bean.getDirName());
        updateBean.setUpdateTime(new Date());
        resourceDirectoryService.updateResourceDirectoryByDirId(updateBean, bean.getDirId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除目录（存在子目录或文件时禁止删除）
     */
    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String dirId) {
        if (StringTools.isEmpty(dirId) || "0".equals(dirId) || "root".equals(dirId)) {
            throw new BusinessException("根目录不能删除");
        }
        ResourceDirectoryQuery childQuery = new ResourceDirectoryQuery();
        childQuery.setParentId(dirId);
        childQuery.setOwnerIdNull(Boolean.TRUE);
        if (resourceDirectoryService.findCountByParam(childQuery) > 0) {
            throw new BusinessException("目录下存在子目录，不能删除");
        }
        ResourceInfoQuery fileQuery = new ResourceInfoQuery();
        fileQuery.setDirectoryId(dirId);
        fileQuery.setOwnerIdNull(Boolean.TRUE);
        if (resourceInfoService.findCountByParam(fileQuery) > 0) {
            throw new BusinessException("目录下存在文件，不能删除");
        }
        resourceDirectoryService.deleteResourceDirectoryByDirId(dirId);
        return getSuccessResponseVO(null);
    }

    /**
     * 同级目录排序
     */
    @PutMapping("/sort")
    public ResponseVO<Void> sort(@RequestBody ResourceDirectorySortDTO dto) {
        if (dto == null || dto.getDirIds() == null || dto.getDirIds().isEmpty()) {
            throw new BusinessException("排序数据不能为空");
        }
        List<ResourceDirectory> list = new ArrayList<>();
        for (int i = 0; i < dto.getDirIds().size(); i++) {
            ResourceDirectory item = new ResourceDirectory();
            item.setDirId(dto.getDirIds().get(i));
            item.setSort(i);
            list.add(item);
        }
        resourceDirectoryService.updateSortBatch(list);
        return getSuccessResponseVO(null);
    }
}
