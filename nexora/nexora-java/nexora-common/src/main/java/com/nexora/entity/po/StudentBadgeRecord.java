package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生徽章解锁记录（uk_user_badge 保证同一徽章只解锁一次）。
 */
@Data
public class StudentBadgeRecord implements Serializable {

    private Long recordId;

    private String userId;

    private String badgeId;

    private Date createTime;
}
