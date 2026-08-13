package com.nexora.admin.controller;

import com.nexora.admin.dto.ResourceMoveDTO;
import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 资源文件管理 Controller
 */
@RestController
@RequestMapping("/resourceInfo")
public class ResourceInfoController extends ABaseController {

    @Resource
    private ResourceInfoService resourceInfoService;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.file-dir}")
    private String resourceFileDir;

    /**
     * 分页查询资源
     */
    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<ResourceInfo>> loadDataList(ResourceInfoQuery query) {
        return getSuccessResponseVO(resourceInfoService.findListByPage(query));
    }

    /**
     * 上传资源
     */
    @PostMapping("/add")
    public ResponseVO<Void> add(@RequestParam("file") MultipartFile file,
                                @RequestParam String resourceName,
                                @RequestParam String resourceType,
                                @RequestParam(required = false) String directoryId,
                                @RequestParam(required = false) String stage) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String relativeDir = resourceFileDir + "/" + dateDir;
        Path targetDir = Paths.get(projectFolder, relativeDir);
        Files.createDirectories(targetDir);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = targetDir.resolve(fileName);
        file.transferTo(target.toFile());

        ResourceInfo bean = new ResourceInfo();
        bean.setResourceId(StringTools.getRandomNumber(Constants.LENGTH_15));
        bean.setResourceName(resourceName);
        bean.setResourceType(resourceType);
        bean.setDirectoryId(directoryId);
        bean.setStage(stage);
        bean.setFilePath(relativeDir + "/" + fileName);
        bean.setFileSize(file.getSize());
        bean.setSource(0);
        bean.setStatus(1);
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        resourceInfoService.add(bean);
        return getSuccessResponseVO(null);
    }

    /**
     * 修改资源（重命名 / 转移等）
     */
    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody ResourceInfo bean) {
        if (StringTools.isEmpty(bean.getResourceId())) {
            throw new BusinessException("资源ID不能为空");
        }
        bean.setUpdateTime(new Date());
        resourceInfoService.updateResourceInfoByResourceId(bean, bean.getResourceId());
        return getSuccessResponseVO(null);
    }

    /**
     * 批量转移文件目录
     */
    @PutMapping("/move")
    public ResponseVO<Void> move(@RequestBody ResourceMoveDTO dto) {
        if (dto == null || dto.getResourceIds() == null || dto.getResourceIds().isEmpty()) {
            throw new BusinessException("请选择要转移的文件");
        }
        if (StringTools.isEmpty(dto.getDirectoryId())) {
            throw new BusinessException("目标目录不能为空");
        }
        Date now = new Date();
        List<ResourceInfo> list = dto.getResourceIds().stream().map(resourceId -> {
            ResourceInfo item = new ResourceInfo();
            item.setResourceId(resourceId);
            item.setDirectoryId(dto.getDirectoryId());
            item.setUpdateTime(now);
            return item;
        }).toList();
        resourceInfoService.updateDirectoryBatch(list);
        return getSuccessResponseVO(null);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String resourceId) {
        resourceInfoService.deleteResourceInfoByResourceId(resourceId);
        return getSuccessResponseVO(null);
    }
}
