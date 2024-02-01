package com.oddfar.campus.business.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson2.JSONObject;
import com.oddfar.campus.business.entity.IUser;
import com.oddfar.campus.common.core.RedisCache;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liuzhaohong on 2024/1/24.
 */
@Slf4j
public class IMTServiceBase {

    @Autowired
    protected RedisCache redisCache;

    protected void setRequest(HttpRequest request, IUser iUser) {
        if (StringUtils.isNotEmpty(iUser.getUserAgent())) {
            request.header("User-Agent", iUser.getUserAgent());
        } else {
            request.header("User-Agent", "iOS;17.3;Apple;?unrecognized?");
        }
        if (StringUtils.isNotEmpty(iUser.getClientUserAgent())) {
            request.header("Client-User-Agent", iUser.getClientUserAgent());
        }
    }

    protected void sleep(int limit) {
        try {
            TimeUnit.SECONDS.sleep((int) (Math.random() * limit));
        } catch (InterruptedException e) {
            log.error("线程睡眠异常:{}", e.getMessage(), e);
        }
    }

    /**
     * 获取i茅台app版本号
     *
     * @return
     */
    public String getMTVersion() {
        String mtVersion = Convert.toStr(redisCache.getCacheObject("mt_version"));
        if (StringUtils.isNotEmpty(mtVersion)) {
            return mtVersion;
        }
        String url = "https://apps.apple.com/cn/app/i%E8%8C%85%E5%8F%B0/id1600482450";
        String htmlContent = HttpUtil.get(url);
        Pattern pattern = Pattern.compile("new__latest__version\">(.*?)</p>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(htmlContent);
        if (matcher.find()) {
            mtVersion = matcher.group(1);
            mtVersion = mtVersion.replace("版本 ", "");
            log.info("getMTVersion:{}", mtVersion);
        }
        redisCache.setCacheObject("mt_version", mtVersion);

        return mtVersion;

    }

    public void query7DaysContinuously(IUser iUser) {
        String url = "https://h5.moutai519.com.cn/game/xmyApplyingReward/7DaysContinuouslyApplyingProgress";
        HttpRequest request = HttpUtil.createRequest(Method.POST, url);
        try{
            request.header("MT-Device-ID", iUser.getDeviceId())
                    .header("MT-APP-Version", getMTVersion())
//                    .header("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 15_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148")
//                    .header("Client-User-Agent", "iOS;15.7;Apple;iPhone XS Max")
                    .cookie("MT-Token-Wap=" + iUser.getCookie() + ";MT-Device-ID-Wap=" + iUser.getDeviceId() + ";");
            this.setRequest(request, iUser);
            String body = request.execute().body();
            log.info("查询7天连续申购:user->{},响应->{}",iUser.getRemark(), body);
            JSONObject jsonObject = JSONObject.parseObject(body);
            if (jsonObject.getInteger("code") != 2000) {
                String message = jsonObject.getString("message");
                throw new ServiceException(message);
            }
            JSONObject data = jsonObject.getJSONObject("data");
            Integer previousProgress = data.getInteger("previousProgress");
            Boolean appliedToday = data.getBoolean("appliedToday");
            if (appliedToday && previousProgress == 6) {
                this.receive7DaysContinuously(iUser);
            }
        }catch (Exception e){
            log.error("查询7天连续申购失败:user->{},失败原因->{}",iUser.getMobile(), e.getMessage());
        }
    }

    public void receive7DaysContinuously(IUser iUser) {
        String url = "https://h5.moutai519.com.cn/game/xmyApplyingReward/receive7DaysContinuouslyApplyingReward";
        HttpRequest request = HttpUtil.createRequest(Method.POST, url);
        try{
            request.header("MT-Device-ID", iUser.getDeviceId())
                    .header("MT-APP-Version", getMTVersion())
                    .cookie("MT-Token-Wap=" + iUser.getCookie() + ";MT-Device-ID-Wap=" + iUser.getDeviceId() + ";");
            this.setRequest(request, iUser);
            String body = request.execute().body();
            log.info("收取7天连续申购奖励:user->{},响应->{}",iUser.getRemark(), body);
            JSONObject jsonObject = JSONObject.parseObject(body);
            if (jsonObject.getInteger("code") != 2000) {
                String message = jsonObject.getString("message");
                throw new ServiceException(message);
            }
        }catch (Exception e){
            log.error("收取7天连续申购奖励失败:user->{},失败原因->{}",iUser.getMobile(), e.getMessage());
        }
    }
}
