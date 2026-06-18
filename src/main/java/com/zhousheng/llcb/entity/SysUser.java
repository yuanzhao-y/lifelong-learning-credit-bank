package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {
    private String username;
    private String passwordHash;
    private String realName;
    private String idCardCipher;
    private String idCardHash;
    private String phoneCipher;
    private String phoneHash;
    private String email;
    private Long avatarFileId;
    private String birthPlace;
    private String currentAddress;
    private String status;
    private LocalDateTime passwordUpdatedAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
