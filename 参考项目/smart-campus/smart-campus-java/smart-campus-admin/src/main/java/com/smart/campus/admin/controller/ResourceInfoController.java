package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.ResourceAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.dto.AddFolderDTO;
import com.smart.campus.entity.dto.MoveResourceDTO;
import com.smart.campus.entity.dto.RenameResourceDTO;
import com.smart.campus.entity.dto.UploadInitDTO;
import com.smart.campus.entity.query.ResourceInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@AdminPermission("resource:manage")
@Validated
@RestController("resourceInfoController")
@RequestMapping("/resourceInfo")
public class ResourceInfoController extends ABaseController {

    private final ResourceAdminBiz resourceAdminBiz;

    public ResourceInfoController(ResourceAdminBiz resourceAdminBiz) {
        this.resourceAdminBiz = resourceAdminBiz;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(ResourceInfoQuery query) {
        return getSuccessResponseVO(resourceAdminBiz.loadDataList(query));
    }

    @RequestMapping("/loadFolderTree")
    public ResponseVO loadFolderTree() {
        return getSuccessResponseVO(resourceAdminBiz.loadFolderTree());
    }

    @RequestMapping("/getResourceListByIds")
    public ResponseVO getResourceListByIds(@NotBlank(message = "资源ID不能为空") String ids) {
        return getSuccessResponseVO(resourceAdminBiz.getResourceListByIds(ids));
    }

    @RequestMapping("/addFolder")
    public ResponseVO addFolder(@Valid AddFolderDTO dto) {
        return getSuccessResponseVO(resourceAdminBiz.addFolder(dto));
    }

    @RequestMapping("/rename")
    public ResponseVO rename(@Valid RenameResourceDTO dto) {
        resourceAdminBiz.rename(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/move")
    public ResponseVO move(@Valid MoveResourceDTO dto) {
        resourceAdminBiz.move(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择要删除的资源") String ids) {
        resourceAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/initUpload")
    public ResponseVO initUpload(@Valid UploadInitDTO dto) {
        return getSuccessResponseVO(resourceAdminBiz.initUpload(dto));
    }

    @RequestMapping("/uploadChunk")
    public ResponseVO uploadChunk(
            @NotBlank(message = "上传ID不能为空") String uploadId,
            @NotNull(message = "分片序号不能为空") Integer chunkIndex,
            @NotNull(message = "分片总数不能为空") Integer chunkCount,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        resourceAdminBiz.uploadChunk(uploadId, chunkIndex, chunkCount, file);
        return getSuccessResponseVO(null);
    }

}
