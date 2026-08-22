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
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目导入：docx 提取（主路径，PDF 转 Word 后导入）；历史 PDF 文本/页图解析保留但已废弃（见 7.18）。
 */
@Service
public class QuestionImportBiz {

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.temp-dir:resource/temp}")
    private String resourceTempDir;

    @Resource
    private ResourceInfoService resourceInfoService;

    /**
     * docx 解析（7.18 主路径）：POI 提取段落与表格文本，作为题目 MD 初稿
     */
    public String parseDocx(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择 Word 文档");
        }
        try (InputStream in = file.getInputStream();
             XWPFDocument document = new XWPFDocument(in)) {
            StringBuilder builder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }
                builder.append(text.trim()).append("\n");
            }
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cells.add(cell.getText() == null ? "" : cell.getText().trim());
                    }
                    builder.append(String.join(" | ", cells)).append("\n");
                }
                builder.append("\n");
            }
            String text = builder.toString().trim();
            if (text.isEmpty()) {
                throw new BusinessException("未能从 Word 文档提取到文本，请确认内容可读取");
            }
            return text;
        } catch (IOException e) {
            throw new BusinessException("Word 文档解析失败：" + e.getMessage());
        }
    }

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
