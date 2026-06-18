package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("learner_work_experience")
@EqualsAndHashCode(callSuper = true)
public class LearnerWorkExperience extends BaseEntity {
    private Long userId;
    private String companyName;
    private String position;
    private LocalDate entryDate;
    private LocalDate leaveDate;
    private String description;
}
