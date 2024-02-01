package com.oddfar.campus.business.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by liuzhaohong on 2024/1/24.
 */
@Data
@TableName("i_travel")
public class ITravel {
    @TableId
    private Long id; // 主键
    private Long mobile; //  I茅台手机号
    private Long userId; // I茅台用户id
    private String remark; // 用户备注
    private BigDecimal xmyReward; // 小茅运奖励,
    private String postcardId; // 明信片编号
    private String postcardName; // 明信片名称
    private Date createTime; // 创建时间
}
