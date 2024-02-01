package com.oddfar.campus.business.service;

import cn.hutool.http.HttpUtil;
import com.oddfar.campus.common.constant.CacheConstants;
import com.oddfar.campus.common.core.RedisCache;
import com.oddfar.campus.common.domain.entity.SysConfigEntity;
import com.oddfar.campus.common.utils.DateUtils;
import com.oddfar.campus.common.utils.StringUtils;
import com.oddfar.campus.framework.manager.AsyncManager;
import com.oddfar.campus.framework.mapper.SysConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PushPlusService {

    @Resource
    private SysConfigMapper configMapper;
    @Resource
    private RedisCache redisCache;

    public void sendNotice(String title, String logContent) {
        Long increment = redisCache.increment(CacheConstants.PUSHPLUS_CNT_KEY);
        redisCache.expire(CacheConstants.PUSHPLUS_CNT_KEY, this.getMidnightSeconds(), TimeUnit.SECONDS);
        if (increment > 150) {
            log.info("超过pushplus限制次数：{}", increment);
            return;
        }
        SysConfigEntity config = new SysConfigEntity();
        config.setConfigKey("pushplusToken");
        SysConfigEntity retConfig = configMapper.selectConfig(config);
        if (retConfig == null || StringUtils.isBlank(retConfig.getConfigValue())) {
            log.error("未配置pushplusToken");
            return;
        }
        String token = retConfig.getConfigValue();
        logContent = logContent + System.lineSeparator() + DateUtils.dateTimeNow(DateUtils.YYYYMMDDHHMMSS);
        AsyncManager.me().execute(sendNotice(token, title, logContent, "txt"));
        if (increment == 150) {
            AsyncManager.me().execute(sendNotice(token,"预警", "消息150次预警", "txt"));
        }
    }

    private long getMidnightSeconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE,1);
        // 获取明天0时的毫秒数
        long midnightMillis = calendar.getTimeInMillis();
        long remainSeconds = (midnightMillis - System.currentTimeMillis()) / 1000;
        return remainSeconds;
    }


    /**
     * push推送
     *
     * @param token    token
     * @param title    消息标题
     * @param content  具体消息内容
     * @param template 发送消息模板
     */
    private TimerTask sendNotice(String token, String title, String content, String template) {
        log.info("消息推送，title:{},content:{}", title, content);
        return new TimerTask() {
            @Override
            public void run() {
                String url = "http://www.pushplus.plus/send";
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                map.put("title", title);
                map.put("content", content);
                if (StringUtils.isEmpty(template)) {
                    map.put("template", "html");
                }
                HttpUtil.post(url, map);
            }
        };
    }
}
