package com.nexora.admin.controller;

import com.nexora.admin.biz.QuestionImportBiz;
import com.nexora.admin.vo.QuestionPdfParseVO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * 题目 PDF 导入 Controller
 */
@RestController
@RequestMapping("/questionImport")
public class QuestionImportController extends ABaseController {

    @Resource
    private QuestionImportBiz questionImportBiz;

    @PostMapping("/parsePdf")
    public ResponseVO<QuestionPdfParseVO> parsePdf(
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String resourceId) {
        return getSuccessResponseVO(questionImportBiz.parsePdf(file, resourceId));
    }

    @PostMapping("/parseDocx")
    public ResponseVO<String> parseDocx(@RequestParam("file") MultipartFile file) {
        return getSuccessResponseVO(questionImportBiz.parseDocx(file));
    }

    @GetMapping("/pageImage/{dir}/{fileName}")
    public ResponseEntity<FileSystemResource> pageImage(@PathVariable String dir,
                                                        @PathVariable String fileName) {
        Path path = questionImportBiz.resolvePageImage(dir, fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(path));
    }
}
