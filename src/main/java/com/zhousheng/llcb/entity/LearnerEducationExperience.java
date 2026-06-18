package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("learner_education_experience")
@EqualsAndHashCode(callSuper = true)
public class LearnerEducationExperience extends BaseEntity {
    private Long userId;
    private String schoolName;
    private String major;
    private String educationLevel;
    private LocalDate enrollmentDate;
    private LocalDate graduationDate;
    private String description;
}
