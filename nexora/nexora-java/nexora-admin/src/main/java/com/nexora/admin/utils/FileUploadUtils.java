package com.nexora.admin.utils;

import com.nexora.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
public class FileUploadUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtils.class);

    private static final String UPLOAD_DIR = "upload";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 保存上传文件到本地
     *
     * @param file          MultipartFile 文件
     * @param projectFolder 项目根目录
     * @return 文件相对路径（如 upload/2026-08-12/xxx.pdf）
     */
    public static String saveFile(MultipartFile file, String projectFolder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        String extension = getFileExtension(originalFilename);
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativePath = UPLOAD_DIR + "/" + dateStr + "/" + fileName;

        String fullPath = projectFolder;
        if (!fullPath.endsWith("/") && !fullPath.endsWith("\\")) {
            fullPath = fullPath + "/";
        }
        fullPath = fullPath + relativePath;

        try {
            Path fullPathObj = Paths.get(fullPath);
            Path parentDir = fullPathObj.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            File destFile = new File(fullPath);
            file.transferTo(destFile);
        } catch (IOException e) {
            logger.error("保存文件失败", e);
            throw new BusinessException("保存文件失败");
        }
        return relativePath;
    }

    /**
     * 删除本地文件
     *
     * @param projectFolder 项目根目录
     * @param relativePath  文件相对路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String projectFolder, String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        String fullPath = projectFolder;
        if (!fullPath.endsWith("/") && !fullPath.endsWith("\\")) {
            fullPath = fullPath + "/";
        }
        fullPath = fullPath + relativePath;
        File file = new File(fullPath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取文件绝对路径
     *
     * @param projectFolder 项目根目录
     * @param relativePath  文件相对路径
     * @return 文件绝对路径
     */
    public static String getFullPath(String projectFolder, String relativePath) {
        String fullPath = projectFolder;
        if (!fullPath.endsWith("/") && !fullPath.endsWith("\\")) {
            fullPath = fullPath + "/";
        }
        return fullPath + relativePath;
    }

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 扩展名（不含点），无扩展名返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex < 0 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
