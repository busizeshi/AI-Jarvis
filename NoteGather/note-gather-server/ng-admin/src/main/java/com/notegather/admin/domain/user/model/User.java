package com.notegather.admin.domain.user.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    @TableField("avatar_url")
    private String avatarUrl;
    private String bio;
    private Integer status;
}
