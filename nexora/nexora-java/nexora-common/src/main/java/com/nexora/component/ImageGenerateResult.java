package com.nexora.component;

/**
 * 文生图调用结果：成功返回图片地址或**直接返回 base64 字节**，失败返回用户可读原因。
 *  编译器自动帮你生成：
 *     1. 构造器: ImageGenerateResult(String imageUrl, String base64, String errorMessage)
 *     2. getter: imageUrl() / base64() / errorMessage()
 *     3. equals / hashCode / toString
 *
 * base64 字段（2026-10-08 新增）：部分网关返回的图片 URL 指向**第三方图床**，
 * 实测 `ccimgapi.rootnode.shop` 连本机与服务器都连不上（HTTP 000）→ 绘本插图全部下载失败。
 * 网关本身支持 `response_format=b64_json`，于是改成直接取字节、不再依赖外部图床。
 */
public record ImageGenerateResult(String imageUrl, String base64, String errorMessage) {

    public static ImageGenerateResult success(String imageUrl) {
        return new ImageGenerateResult(imageUrl, null, null);
    }

    /** 直接拿字节（跳过外部图床下载） */
    public static ImageGenerateResult successBase64(String base64) {
        return new ImageGenerateResult(null, base64, null);
    }

    public static ImageGenerateResult failure(String errorMessage) {
        return new ImageGenerateResult(null, null, errorMessage);
    }

    public boolean success() {
        return (imageUrl != null && !imageUrl.isBlank()) || (base64 != null && !base64.isBlank());
    }

    /** 是否已带图片字节（调用方优先用它，省一次外部下载） */
    public boolean hasBase64() {
        return base64 != null && !base64.isBlank();
    }
}
