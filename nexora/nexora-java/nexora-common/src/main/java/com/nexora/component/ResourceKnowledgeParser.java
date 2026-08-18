package com.nexora.component;

import com.nexora.entity.po.ResourceInfo;
import com.nexora.exception.BusinessException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hslf.extractor.QuickButCruddyTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 资源文件文本解析：txt/md/docx/doc/ppt/pptx/pdf。
 */
@Component
public class ResourceKnowledgeParser {

    @Value("${project.folder}")
    private String projectFolder;

    /**
     * 按文件扩展名解析资源正文，附带解析提示。
     */
    public ParseResult parse(ResourceInfo resource) {
        if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
            throw new BusinessException("资源不存在或暂不可用");
        }
        if (resource.getFilePath() == null || resource.getFilePath().isBlank()) {
            throw new BusinessException("资源文件地址为空，无法解析");
        }
        Path root = Paths.get(projectFolder).toAbsolutePath().normalize();
        Path file = Paths.get(projectFolder, resource.getFilePath()).toAbsolutePath().normalize();
        if (!file.startsWith(root) || !Files.exists(file) || !Files.isRegularFile(file)) {
            throw new BusinessException("资源文件不存在");
        }

        String extension = extensionOf(resource);
        List<String> warnings = new ArrayList<>();
        String text;
        try {
            switch (extension) {
                case "txt", "md", "markdown" -> text = readTextFile(file);
                case "docx" -> text = parseDocx(file);
                case "doc" -> {
                    text = parseDoc(file);
                    warnings.add("检测到老版 .doc 格式，复杂排版可能丢失内容，建议转成 .docx 后重新导入");
                }
                case "pptx" -> {
                    text = parsePptx(file);
                    warnings.add("PPT 中的图片、图表文字无法自动提取，如需补充可切换手动填写资源说明");
                }
                case "ppt" -> {
                    text = parsePpt(file);
                    warnings.add("PPT 中的图片、图表文字无法自动提取，如需补充可切换手动填写资源说明");
                }
                case "pdf" -> {
                    PdfText pdfText = parsePdf(file);
                    text = pdfText.getText();
                    if (pdfText.isScannedLike()) {
                        warnings.add("该 PDF 疑似扫描版，提取文字较少，建议转成文字型 PDF 或手动填写资源说明");
                    }
                }
                default -> throw new BusinessException("该文件类型暂不支持自动解析，可在导入抽屉中手动填写资源说明");
            }
        } catch (IOException e) {
            throw new BusinessException("资源解析失败: " + e.getMessage());
        }

        text = cleanText(text);
        if (text.isBlank()) {
            throw new BusinessException("未能从资源中提取有效文字，可切换手动填写资源说明");
        }
        ParseResult result = new ParseResult();
        result.setText(text);
        result.setWarnings(warnings);
        result.setFileName(resource.getResourceName());
        result.setExtension(extension);
        return result;
    }

    private String readTextFile(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, Charset.forName("GBK"));
        }
    }

    private String parseDocx(Path file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream in = Files.newInputStream(file);
             XWPFDocument document = new XWPFDocument(in)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                appendLine(builder, paragraph.getText());
            }
            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cells.add(cell.getText().trim());
                    }
                    builder.append(String.join("\t", cells)).append('\n');
                }
            }
        }
        return builder.toString();
    }

    private String parseDoc(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file);
             WordExtractor extractor = new WordExtractor(in)) {
            return extractor.getText();
        }
    }

    private String parsePptx(Path file) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (InputStream in = Files.newInputStream(file);
             XMLSlideShow slideShow = new XMLSlideShow(in)) {
            int index = 1;
            for (XSLFSlide slide : slideShow.getSlides()) {
                builder.append("## 第 ").append(index).append(" 页\n");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        appendLine(builder, textShape.getText());
                    }
                }
                index++;
            }
        }
        return builder.toString();
    }

    private String parsePpt(Path file) throws IOException {
        QuickButCruddyTextExtractor extractor = null;
        try (InputStream in = Files.newInputStream(file)) {
            extractor = new QuickButCruddyTextExtractor(in);
            return extractor.getTextAsString();
        } finally {
            if (extractor != null) {
                extractor.close();
            }
        }
    }

    private PdfText parsePdf(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            int pages = Math.max(1, document.getNumberOfPages());
            int meaningful = text == null ? 0 : text.replaceAll("\\s+", "").length();
            PdfText result = new PdfText();
            result.setText(text == null ? "" : text);
            result.setScannedLike(meaningful == 0 || meaningful < pages * 20);
            return result;
        }
    }

    private String extensionOf(ResourceInfo resource) {
        String name = resource.getResourceName();
        if (name == null || !name.contains(".")) {
            name = resource.getFilePath();
        }
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void appendLine(StringBuilder builder, String text) {
        if (text != null && !text.isBlank()) {
            builder.append(text.trim()).append('\n');
        }
    }

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.replace('\u0000', ' ')
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("\\n{3,}", "\n\n");
        return cleaned.trim();
    }

    /**
     * 解析结果。
     */
    public static class ParseResult {
        private String text;
        private String fileName;
        private String extension;
        private List<String> warnings = new ArrayList<>();

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }

    private static class PdfText {
        private String text;
        private boolean scannedLike;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isScannedLike() {
            return scannedLike;
        }

        public void setScannedLike(boolean scannedLike) {
            this.scannedLike = scannedLike;
        }
    }
}
