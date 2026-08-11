package com.smart.campus.web.entity.vo.message;

import java.io.Serializable;

public class MessageCenterContactVO implements Serializable {

    private String name;
    private String meta;
    private String preview;
    private String timeText;
    private Boolean unread;
    private String avatar;
    private String avatarTheme;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public Boolean getUnread() {
        return unread;
    }

    public void setUnread(Boolean unread) {
        this.unread = unread;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarTheme() {
        return avatarTheme;
    }

    public void setAvatarTheme(String avatarTheme) {
        this.avatarTheme = avatarTheme;
    }
}
