package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.PaperAdminBiz;
import com.smart.campus.admin.entity.dto.PaperSaveDTO;
import com.smart.campus.admin.entity.dto.PaperStructureSaveDTO;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.query.PaperInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("teaching:paper")
@Validated
@RestController("paperInfoController")
@RequestMapping("/paperInfo")
public class PaperInfoController extends ABaseController {

    private final PaperAdminBiz paperAdminBiz;

    public PaperInfoController(PaperAdminBiz paperAdminBiz) {
        this.paperAdminBiz = paperAdminBiz;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(PaperInfoQuery query) {
        return getSuccessResponseVO(paperAdminBiz.loadDataList(query));
    }

    @RequestMapping("/getPaperInfoById")
    public ResponseVO getPaperInfoById(@NotBlank(message = "试卷ID不能为空") String paperId) {
        return getSuccessResponseVO(paperAdminBiz.getPaperInfoById(paperId));
    }

    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated(PaperSaveDTO.Create.class) PaperSaveDTO dto) {
        return getSuccessResponseVO(paperAdminBiz.add(dto));
    }

    @RequestMapping("/updatePaperInfoById")
    public ResponseVO updatePaperInfoById(@RequestBody @Validated(PaperSaveDTO.Update.class) PaperSaveDTO dto) {
        paperAdminBiz.updatePaperInfoById(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/saveStructure")
    public ResponseVO saveStructure(@RequestBody @Validated PaperStructureSaveDTO dto) {
        paperAdminBiz.saveStructure(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deletePaperInfoById")
    public ResponseVO deletePaperInfoById(@NotBlank(message = "试卷ID不能为空") String paperId) {
        paperAdminBiz.deletePaperInfoById(paperId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择需要删除的试卷") String ids) {
        paperAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }
}
