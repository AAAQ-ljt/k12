package com.nexora.admin.biz;

import com.nexora.admin.vo.QuestionPdfPageVO;
import com.nexora.admin.vo.QuestionPdfParseVO;
import com.nexora.constants.Constants;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 题目导入：PDFBox 抽取文本 + PDFRenderer 渲染页图。
 * TODO: 当前解析格式不完善，后续重构或移交。
 */
@Service
public class QuestionImportBiz {

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.temp-dir:resource/temp}")
    private String resourceTempDir;

    @Resource
    private ResourceInfoService resourceInfoService;

    public QuestionPdfParseVO parsePdf(MultipartFile file, String resourceId) {
        File pdfFile = null;
        boolean fromUpload = resourceId == null || resourceId.isEmpty();
        try {
            if (fromUpload) {
                if (file == null || file.isEmpty()) {
                    throw new BusinessException("请选择 PDF 文件");
                }
                pdfFile = File.createTempFile("question-pdf-", ".pdf");
                file.transferTo(pdfFile);
            } else {
                ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
                if (resource == null) {
                    throw new BusinessException("资源不存在");
                }
                pdfFile = Paths.get(projectFolder, resource.getFilePath()).toFile();
                if (!pdfFile.exists()) {
                    throw new BusinessException("PDF 文件不存在");
                }
            }
            try (PDDocument document = Loader.loadPDF(pdfFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(document);
                String dirName = StringTools.getRandomNumber(Constants.LENGTH_15);
                Path imageDir = Paths.get(projectFolder, resourceTempDir, "question-pdf", dirName);
                Files.createDirectories(imageDir);
                PDFRenderer renderer = new PDFRenderer(document);
                List<QuestionPdfPageVO> pages = new ArrayList<>();
                int pageCount = document.getNumberOfPages();
                for (int i = 0; i < pageCount; i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 120);
                    String fileName = "page_" + (i + 1) + ".png";
                    ImageIO.write(image, "png", imageDir.resolve(fileName).toFile());
                    pages.add(new QuestionPdfPageVO(i + 1,
                            "/api/questionImport/pageImage/" + dirName + "/" + fileName));
                }
                QuestionPdfParseVO vo = new QuestionPdfParseVO();
                vo.setText(text == null ? "" : text);
                vo.setPages(pages);
                return vo;
            }
        } catch (IOException e) {
            throw new BusinessException("PDF 解析失败：" + e.getMessage());
        } finally {
            if (fromUpload && pdfFile != null) {
                pdfFile.delete();
            }
        }
    }

    public Path resolvePageImage(String dir, String fileName) {
        if (dir == null || fileName == null
                || !dir.matches("[A-Za-z0-9_-]+")
                || !fileName.matches("[A-Za-z0-9_-]+\\.png")) {
            throw new BusinessException("非法的页图参数");
        }
        Path base = Paths.get(projectFolder, resourceTempDir, "question-pdf").normalize();
        Path target = base.resolve(dir).resolve(fileName).normalize();
        if (!target.startsWith(base) || !Files.exists(target)) {
            throw new BusinessException("页图不存在");
        }
        return target;
    }
}
