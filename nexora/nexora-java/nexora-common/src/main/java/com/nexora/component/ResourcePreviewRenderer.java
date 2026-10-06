package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.entity.vo.ResourcePreviewMetaVO;
import jakarta.annotation.Resource;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 资源在线预览产物生成器（Office 文档 → PDF → 逐页 JPEG）。
 *
 * 为什么需要：管理端此前用 iframe 直接打开原始文件流，浏览器无法内联渲染 pptx/docx 会变成下载；
 * 学生端用 jit-viewer 在浏览器里解压渲染，遇到 88MB（内含单张 93MB TIFF）的课件要先把整包拉下来再解压，
 * 弱网下就是长时间转圈。这里改为「服务端预转换 + 前端按页拉图」，首屏只加载第 1 页。
 *
 * 产物（与原文件同级、不动数据库、不加字段，路径由 file_path 推导）：
 * &lt;目录&gt;/&lt;文件名去扩展名&gt;_preview/
 *   ├── preview.pdf      LibreOffice 转换结果（供下载/打印/降级预览）
 *   ├── page_001.jpg…    PDFBox 逐页渲染（宽约 preview-image-width，JPEG）
 *   └── meta.json        ResourcePreviewMetaVO 序列化（状态/页数/尝试次数）
 *
 * 依赖现状（服务器已具备，无需新增安装）：LibreOffice 7.3（soffice）+ Noto Sans CJK 中文字体 +
 * PDFBox 3.0.3（nexora-common 既有依赖，QuestionImportBiz 已有同样的逐页渲染先例）。
 */
@Component
public class ResourcePreviewRenderer {

    private static final Logger log = LoggerFactory.getLogger(ResourcePreviewRenderer.class);

    /** 支持预转换的 Office 扩展名（pdf 本来就能内联预览、视频走 HLS，都不在此列） */
    private static final Set<String> OFFICE_EXTENSIONS = Set.of("ppt", "pptx", "doc", "docx", "xls", "xlsx");

    private static final String PREVIEW_DIR_SUFFIX = "_preview";
    private static final String META_FILE = "meta.json";
    private static final String PDF_FILE = "preview.pdf";
    private static final int MAX_ATTEMPTS = 3;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.soffice-path:soffice}")
    private String sofficePath;

    /** 预览图目标宽度（像素）：1600 宽在 16:9 屏幕上清晰且单页 JPEG 约 100-300KB */
    @Value("${resource.preview-image-width:1600}")
    private int previewImageWidth;

    /** 单次转换超时（秒）：这几个课件内嵌大图多，留足时间；超时强杀避免占死队列 */
    @Value("${resource.preview-timeout-seconds:300}")
    private long previewTimeoutSeconds;

    @Resource
    private ResourceHeavyJobLock resourceHeavyJobLock;

    /**
     * 是否需要/可以做预转换（按原文件名扩展名判断）
     */
    public boolean isOfficeDocument(String fileName) {
        return OFFICE_EXTENSIONS.contains(extensionOf(fileName));
    }

    /**
     * 文件的扩展名（小写、不含点）；无扩展名返回空串
     */
    public String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 源文件绝对路径（相对 project.folder）
     */
    public Path resolveSourcePath(String relativeFilePath) {
        return Paths.get(projectFolder, relativeFilePath).toAbsolutePath().normalize();
    }

    /**
     * 预览产物目录：与原文件同级、<文件名去扩展名>_preview
     */
    public Path resolvePreviewDir(String relativeFilePath) {
        Path source = resolveSourcePath(relativeFilePath);
        String fileName = source.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String baseName = dot > 0 ? fileName.substring(0, dot) : fileName;
        return source.resolveSibling(baseName + PREVIEW_DIR_SUFFIX);
    }

    /**
     * 单页图片的绝对路径；页码越界或产物不存在返回 null
     */
    public Path resolvePageImage(String relativeFilePath, int page) {
        if (page < 1) {
            return null;
        }
        Path previewDir = resolvePreviewDir(relativeFilePath);
        Path imagePath = previewDir.resolve(String.format("page_%03d.jpg", page)).normalize();
        if (!imagePath.startsWith(previewDir) || !Files.exists(imagePath)) {
            return null;
        }
        return imagePath;
    }

    /**
     * 预览 PDF（转换产物）的绝对路径；未生成返回 null
     */
    public Path resolvePreviewPdf(String relativeFilePath) {
        Path pdfPath = resolvePreviewDir(relativeFilePath).resolve(PDF_FILE).normalize();
        if (!Files.exists(pdfPath)) {
            return null;
        }
        return pdfPath;
    }

    /**
     * 读取预览元信息；未生成过返回 null（损坏的 meta.json 视为未生成）
     */
    public ResourcePreviewMetaVO readMeta(String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.isEmpty()) {
            return null;
        }
        Path metaPath = resolvePreviewDir(relativeFilePath).resolve(META_FILE);
        if (!Files.exists(metaPath)) {
            return null;
        }
        try {
            String json = Files.readString(metaPath, StandardCharsets.UTF_8);
            return JSON.parseObject(json, ResourcePreviewMetaVO.class);
        } catch (Exception e) {
            log.warn("读取预览元信息失败 path={}", metaPath, e);
            return null;
        }
    }

    /**
     * 已经生成好且可用的预览页数；未就绪返回 0
     */
    public int readReadyPages(String relativeFilePath) {
        ResourcePreviewMetaVO meta = readMeta(relativeFilePath);
        if (meta == null || !ResourcePreviewMetaVO.STATUS_READY.equals(meta.getStatus())) {
            return 0;
        }
        return meta.getPages() == null ? 0 : meta.getPages();
    }

    /**
     * 是否需要（重新）生成：未生成过，或失败次数未达上限（成功过的不再重做）
     */
    public boolean needRender(String relativeFilePath) {
        ResourcePreviewMetaVO meta = readMeta(relativeFilePath);
        if (meta == null) {
            return true;
        }
        if (ResourcePreviewMetaVO.STATUS_READY.equals(meta.getStatus())) {
            return false;
        }
        if (ResourcePreviewMetaVO.STATUS_GENERATING.equals(meta.getStatus())) {
            // 生成中且超过 30 分钟仍未收敛（进程被杀等），允许重来一次
            return true;
        }
        int attempts = meta.getAttempts() == null ? 0 : meta.getAttempts();
        return attempts < MAX_ATTEMPTS;
    }

    /**
     * 生成预览产物（耗时操作，调用方必须保证串行且已持重活锁）；成功返回 true
     */
    public boolean render(String relativeFilePath) {
        Path source = resolveSourcePath(relativeFilePath);
        Path previewDir = resolvePreviewDir(relativeFilePath);
        int attempts = nextAttempts(previewDir);
        long sourceSize = 0L;
        try {
            if (!Files.exists(source)) {
                writeMeta(previewDir, ResourcePreviewMetaVO.STATUS_FAILED, 0, attempts, sourceSize, "源文件不存在");
                return false;
            }
            sourceSize = Files.size(source);
            Files.createDirectories(previewDir);
            writeMeta(previewDir, ResourcePreviewMetaVO.STATUS_GENERATING, 0, attempts, sourceSize, null);

            long begin = System.currentTimeMillis();
            Path pdfPath = convertToPdf(source, previewDir);
            int pages = renderPdfToImages(pdfPath, previewDir);
            writeMeta(previewDir, ResourcePreviewMetaVO.STATUS_READY, pages, attempts, sourceSize, null);
            log.info("资源预览生成完成 file={} pages={} 耗时={}ms", relativeFilePath, pages,
                    System.currentTimeMillis() - begin);
            return true;
        } catch (Exception e) {
            log.warn("资源预览生成失败 file={} attempts={}", relativeFilePath, attempts, e);
            writeMeta(previewDir, ResourcePreviewMetaVO.STATUS_FAILED, 0, attempts, sourceSize,
                    trimMessage(e.getMessage()));
            return false;
        }
    }

    /**
     * LibreOffice headless 转 PDF：独立 UserInstallation 避免单实例锁，超时强杀
     */
    private Path convertToPdf(Path source, Path previewDir) throws IOException, InterruptedException {
        Path expected = previewDir.resolve(PDF_FILE);
        Files.deleteIfExists(expected);
        String profile = "file:///tmp/nexora-lo-" + UUID.randomUUID().toString().replace("-", "");
        List<String> command = List.of(sofficePath, "--headless", "--norestore", "--nolockcheck", "--nologo",
                "-env:UserInstallation=" + profile,
                "--convert-to", "pdf", "--outdir", previewDir.toString(), source.toString());
        runCommand(command, previewTimeoutSeconds);

        // LibreOffice 输出名为 <源文件名>.pdf，统一改名为 preview.pdf
        String convertedName = baseName(source.getFileName().toString()) + ".pdf";
        Path converted = previewDir.resolve(convertedName);
        if (!Files.exists(converted)) {
            throw new IOException("转换产物缺失: " + convertedName);
        }
        Files.move(converted, expected, StandardCopyOption.REPLACE_EXISTING);
        return expected;
    }

    /**
     * PDF 逐页渲染为 JPEG（宽度对齐 preview-image-width，白底 RGB 避免 JPEG 透明变黑）
     */
    private int renderPdfToImages(Path pdfPath, Path previewDir) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                float pageWidthPt = document.getPage(i).getMediaBox().getWidth();
                float dpi = pageWidthPt > 0 ? previewImageWidth * 72f / pageWidthPt : 144f;
                dpi = Math.max(72f, Math.min(dpi, 200f));
                // ImageType.RGB：白底不透明，避免 JPEG 把透明像素写成黑色
                BufferedImage image = renderer.renderImageWithDPI(i, dpi, ImageType.RGB);
                Path pagePath = previewDir.resolve(String.format("page_%03d.jpg", i + 1));
                ImageIO.write(image, "jpg", pagePath.toFile());
                image.flush();
            }
            return pageCount;
        }
    }

    /**
     * 执行外部命令并等待结束；超时强杀（与 ResourceUploadServiceImpl#executeCommand 同构）
     */
    private void runCommand(List<String> command, long timeoutSeconds) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
            try (InputStream input = process.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "";
            }
        });
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("文档转换超时(" + timeoutSeconds + "s)");
        }
        if (process.exitValue() != 0) {
            String output = "";
            try {
                output = outputFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
                // 输出读取失败不影响退出码判断
            }
            String message = output.length() > 500 ? output.substring(output.length() - 500) : output;
            throw new IOException("文档转换失败: " + message);
        }
    }

    private void writeMeta(Path previewDir, String status, int pages, int attempts, long sourceSize, String message) {
        try {
            Files.createDirectories(previewDir);
            ResourcePreviewMetaVO meta = new ResourcePreviewMetaVO();
            meta.setStatus(status);
            meta.setPages(pages);
            meta.setAttempts(attempts);
            meta.setSourceSize(sourceSize);
            meta.setMessage(message);
            if (ResourcePreviewMetaVO.STATUS_READY.equals(status)) {
                meta.setGeneratedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }
            Path tmp = previewDir.resolve(META_FILE + ".tmp");
            Files.writeString(tmp, JSON.toJSONString(meta), StandardCharsets.UTF_8);
            Files.move(tmp, previewDir.resolve(META_FILE), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.warn("写入预览元信息失败 dir={}", previewDir, e);
        }
    }

    private int nextAttempts(Path previewDir) {
        try {
            Path metaPath = previewDir.resolve(META_FILE);
            if (!Files.exists(metaPath)) {
                return 1;
            }
            ResourcePreviewMetaVO meta = JSON.parseObject(Files.readString(metaPath, StandardCharsets.UTF_8),
                    ResourcePreviewMetaVO.class);
            return (meta == null || meta.getAttempts() == null ? 0 : meta.getAttempts()) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private String baseName(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }

    private String trimMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    /**
     * 供调用方判断能否安全执行转换（源文件必须位于 project.folder 内）
     */
    public boolean isInsideProjectFolder(Path path) {
        Path root = Paths.get(projectFolder).toAbsolutePath().normalize();
        return path.toAbsolutePath().normalize().startsWith(root);
    }

    /**
     * 重活锁：预览转换期间持锁，避免与视频转码同时吃内存
     */
    public boolean tryLockHeavy(String owner) {
        return resourceHeavyJobLock.tryLock(owner);
    }
}
