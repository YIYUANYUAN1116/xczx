package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService extends IService<CourseTeacher> {
    /**
     * 查询 courseId 对应的教师
     * @param courseId
     * @return
     */
    List<CourseTeacherDto> getCourseTeacherByCourseId(Long courseId);

    /**
     * 添加或更新教师信息
     * @param courseTeacherDto
     * @return
     */
    CourseTeacherDto addCourseTeacher(CourseTeacherDto courseTeacherDto);

    /**
     * 删除教师
     * @param courseId
     * @param teacherId
     * @return
     */
    void deleteTeacher(Long courseId, Long teacherId);
}
