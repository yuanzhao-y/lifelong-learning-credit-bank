package com.zhousheng.llcb.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhousheng.llcb.common.ApiResponse;
import com.zhousheng.llcb.entity.LearnerEducationExperience;
import com.zhousheng.llcb.entity.LearnerWorkExperience;
import com.zhousheng.llcb.mapper.LearnerEducationExperienceMapper;
import com.zhousheng.llcb.mapper.LearnerWorkExperienceMapper;
import com.zhousheng.llcb.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final LearnerEducationExperienceMapper educationMapper;
    private final LearnerWorkExperienceMapper workMapper;

    public ProfileController(LearnerEducationExperienceMapper educationMapper,
                             LearnerWorkExperienceMapper workMapper) {
        this.educationMapper = educationMapper;
        this.workMapper = workMapper;
    }

    @GetMapping("/educations")
    public ApiResponse<List<LearnerEducationExperience>> educations() {
        return ApiResponse.ok(educationMapper.selectList(new LambdaQueryWrapper<LearnerEducationExperience>()
                .eq(LearnerEducationExperience::getUserId, SecurityUtils.currentUserId())
                .orderByDesc(LearnerEducationExperience::getEnrollmentDate)));
    }

    @PostMapping("/educations")
    public ApiResponse<LearnerEducationExperience> createEducation(@RequestBody LearnerEducationExperience education) {
        education.setUserId(SecurityUtils.currentUserId());
        educationMapper.insert(education);
        return ApiResponse.ok(education);
    }

    @PutMapping("/educations/{id}")
    public ApiResponse<Void> updateEducation(@PathVariable Long id, @RequestBody LearnerEducationExperience education) {
        education.setId(id);
        education.setUserId(SecurityUtils.currentUserId());
        educationMapper.updateById(education);
        return ApiResponse.ok();
    }

    @DeleteMapping("/educations/{id}")
    public ApiResponse<Void> deleteEducation(@PathVariable Long id) {
        educationMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/works")
    public ApiResponse<List<LearnerWorkExperience>> works() {
        return ApiResponse.ok(workMapper.selectList(new LambdaQueryWrapper<LearnerWorkExperience>()
                .eq(LearnerWorkExperience::getUserId, SecurityUtils.currentUserId())
                .orderByDesc(LearnerWorkExperience::getEntryDate)));
    }

    @PostMapping("/works")
    public ApiResponse<LearnerWorkExperience> createWork(@RequestBody LearnerWorkExperience work) {
        work.setUserId(SecurityUtils.currentUserId());
        workMapper.insert(work);
        return ApiResponse.ok(work);
    }

    @PutMapping("/works/{id}")
    public ApiResponse<Void> updateWork(@PathVariable Long id, @RequestBody LearnerWorkExperience work) {
        work.setId(id);
        work.setUserId(SecurityUtils.currentUserId());
        workMapper.updateById(work);
        return ApiResponse.ok();
    }

    @DeleteMapping("/works/{id}")
    public ApiResponse<Void> deleteWork(@PathVariable Long id) {
        workMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
