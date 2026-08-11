package com.smart.campus.web.task;

import com.smart.campus.web.biz.CourseWebBiz;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CourseStudyProgressFlushTask {

    private static final Logger logger = LoggerFactory.getLogger(CourseStudyProgressFlushTask.class);

    @Resource
    private CourseWebBiz courseWebBiz;

    @Scheduled(cron = "0 * * * * ?")
    public void flushStudyProgress() {
        try {
            courseWebBiz.flushStudyProgressCacheToDb();
        } catch (Exception exception) {
            logger.error("Flush course study progress cache failed", exception);
        }
    }
}
