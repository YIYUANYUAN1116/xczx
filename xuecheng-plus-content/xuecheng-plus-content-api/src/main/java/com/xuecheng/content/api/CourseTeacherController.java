package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "教师编辑接口",tags = "教师编辑接口")
@Slf4j
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation(value = "根据courseId查询教师")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacherDto> getCourseTeacherByCourseId(@PathVariable Long courseId){

        return courseTeacherService.getCourseTeacherByCourseId(courseId);
    }

    @ApiOperation(value = "添加教师")
    @PostMapping("/courseTeacher")
    public CourseTeacherDto addCourseTeacher(@RequestBody CourseTeacherDto courseTeacherDto){
        return courseTeacherService.addCourseTeacher(courseTeacherDto);
    }

    @ApiOperation(value = "删除教师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void addCourseTeacher(@PathVariable Long courseId,@PathVariable Long teacherId){
        //todo Long companyId = 1L;
        courseTeacherService.deleteTeacher(courseId,teacherId);
    }
}
