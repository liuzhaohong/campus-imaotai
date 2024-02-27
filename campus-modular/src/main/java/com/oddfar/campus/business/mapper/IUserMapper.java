package com.oddfar.campus.business.mapper;

import com.oddfar.campus.business.entity.IUser;
import com.oddfar.campus.common.core.BaseMapperX;
import com.oddfar.campus.common.core.LambdaQueryWrapperX;
import com.oddfar.campus.common.domain.PageResult;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * I茅台用户Mapper接口
 *
 * @author oddfar
 * @date 2023-07-02
 */
public interface IUserMapper extends BaseMapperX<IUser> {
    default PageResult<IUser> selectPage(IUser iUser) {

        return selectPage(new LambdaQueryWrapperX<IUser>()
                .eqIfPresent(IUser::getUserId, iUser.getUserId())
                .eqIfPresent(IUser::getMobile, iUser.getMobile())
                .eqIfPresent(IUser::getProvinceName, iUser.getProvinceName())
                .betweenIfPresent(IUser::getExpireTime, iUser.getParams())
                .orderByAsc(IUser::getCreateTime)
        );

    }

    default PageResult<IUser> selectPage(IUser iUser, Long userId) {

        return selectPage(new LambdaQueryWrapperX<IUser>()
                .eqIfPresent(IUser::getUserId, iUser.getUserId())
                .eqIfPresent(IUser::getMobile, iUser.getMobile())
                .eqIfPresent(IUser::getProvinceName, iUser.getProvinceName())
                .eq(IUser::getCreateUser, userId)
                .betweenIfPresent(IUser::getExpireTime, iUser.getParams())
                .orderByAsc(IUser::getCreateTime)
        );

    }

    default List<IUser> selectReservationUserOrderByMinute() {
        return selectList(new LambdaQueryWrapperX<IUser>()
//                      .gt(IUser::getExpireTime, new Date())
                        .ne(IUser::getLat, "")
                        .ne(IUser::getLng, "")
                        .ne(IUser::getItemCode, "")
                        .isNotNull(IUser::getItemCode)
                        .orderByAsc(IUser::getMinute)

        );

    }

    /**
     * 通过预约执行分钟查询预约用户列表
     */
    default List<IUser> selectReservationUserByMinute(int minute) {
        return selectList(new LambdaQueryWrapperX<IUser>()
                        .eq(IUser::getMinute, minute)
//                      .gt(IUser::getExpireTime, new Date())
                        .ne(IUser::getLat, "")
                        .ne(IUser::getLng, "")
                        .ne(IUser::getItemCode, "")
                        .isNotNull(IUser::getItemCode)
                        .eq(IUser::getDelFlag, false)
        );
    }

    // 生成随机数，1<=随机数<=30
    @Select("UPDATE i_user SET `minute` = (SELECT FLOOR(RAND() * 30 + 1)) WHERE random_minute = \"0\"")
    void updateUserMinuteBatch();

    int deleteIUser(Long[] iUserId);

    @Update("UPDATE i_user SET `del_flag` = true WHERE mobile = ${mobiles}")
    int logicRemoveUser(@Param("mobiles") Long mobiles);
}
