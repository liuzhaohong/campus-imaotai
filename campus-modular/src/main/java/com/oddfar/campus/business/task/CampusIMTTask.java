package com.oddfar.campus.business.task;

import com.oddfar.campus.business.service.IMTService;
import com.oddfar.campus.business.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * i茅台定时任务
 */
@Configuration
@EnableScheduling
public class CampusIMTTask {
    private static final Logger logger = LoggerFactory.getLogger(CampusIMTTask.class);

    @Autowired
    private IMTService imtService;
    @Autowired
    private IUserService iUserService;

    /**
     * 8:58、9:58、17:58 批量修改用户随机预约的时间
     */
    @Async
    @Scheduled(cron = "0 58 8,9,17 ? * * ")
    public void updateUserMinuteBatch() {
        iUserService.updateUserMinuteBatch();
    }

    /**
     * 8:55 推送比例
     */
    @Async
    @Scheduled(cron = "0 55 8 ? * * ")
    public void getRateAndPushPlus() {
        imtService.getRateAndPushPlus();
    }

    /**
     * 10点期间，每分钟执行一次批量获得旅行奖励
     */
    @Async
    @Scheduled(cron = "0 0/1 10 ? * *")
    public void getTravelRewardBatch() {
        imtService.getTravelRewardBatch();

    }

    /**
     * 9点期间，每分钟执行一次
     */
    @Async
    @Scheduled(cron = "0 0/1 9 ? * *")
    public void reservationBatchTask() {
        imtService.reservationBatch();

    }


    @Async
    @Scheduled(cron = "0 28,51 8 ? * * ")
    public void refresh() {
        logger.info("「刷新数据」开始刷新版本号，预约item，门店shop列表  ");
        imtService.refreshAll();

    }


    /**
     * 18点 每分钟获取申购结果
     */
    @Async
    @Scheduled(cron = "0 0/1 18 ? * *")
    public void appointmentResults() {
        imtService.appointmentResults();
    }


}