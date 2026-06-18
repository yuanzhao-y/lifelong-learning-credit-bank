package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("expert_profile")
@EqualsAndHashCode(callSuper = true)
public class ExpertProfile extends BaseEntity {
    private Long userId;
    private String expertName;
    private String title;
    private String professionalDirection;
    private String contactPhoneCipher;
    private String contactPhoneHash;
    private String email;
    private String status;
}
