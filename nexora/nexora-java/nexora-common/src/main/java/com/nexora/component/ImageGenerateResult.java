package com.nexora.component;

/**
 * 文生图调用结果：成功返回图片 URL，失败返回用户可读原因。
 *  编译器自动帮你生成：
 *     1. 构造器: ImageGenerateResult(String imageUrl, String errorMessage)
 *     2. getter: imageUrl() 和 errorMessage()
 *     3. equals / hashCode / toString
 */
public record ImageGenerateResult(String imageUrl, String errorMessage) {

    public static ImageGenerateResult success(String imageUrl) {
        return new ImageGenerateResult(imageUrl, null);
    }

    public static ImageGenerateResult failure(String errorMessage) {
        return new ImageGenerateResult(null, errorMessage);
    }

    public boolean success() {
        return imageUrl != null && !imageUrl.isBlank();
    }
}
