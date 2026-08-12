package com.easymall.task;

import com.easymall.entity.enums.DateTimePatternEnum;
import com.easymall.service.StatisticsInfoService;
import com.easymall.utils.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTask {

    @Resource
    private StatisticsInfoService statisticsInfoService;

    @Scheduled(cron = "0 0 1 * * ?") //每天1点执行
    public void statisticsTask() {
        String yesterday = DateUtil.getBeforeDay(1, DateTimePatternEnum.YYYY_MM_DD.getPattern());
        statisticsInfoService.statisticsData(yesterday);
    }
}