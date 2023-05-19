package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.XueChengException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {


    @Override
    public List<CourseTeacherDto> getCourseTeacherByCourseId(Long courseId) {
        if (courseId == null || courseId <=0){
            XueChengException.cast(CommonError.PARAMS_ERROR);
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeachers = this.baseMapper.selectList(queryWrapper);
        if (courseTeachers.isEmpty()){
            return null;
        }
        List<CourseTeacherDto> courseTeacherReturn = new ArrayList<>();
        courseTeachers.forEach(courseTeacher -> {
            CourseTeacherDto courseTeacherDto = new CourseTeacherDto();
            BeanUtils.copyProperties(courseTeacher,courseTeacherDto);
            courseTeacherReturn.add(courseTeacherDto);
        });
        return courseTeacherReturn;
    }

    @Override
    public CourseTeacherDto addCourseTeacher(CourseTeacherDto courseTeacherDto) {
        //参数校验
        if (courseTeacherDto.getCourseId() == null){
            XueChengException.cast(CommonError.PARAMS_ERROR);
        }
        if (courseTeacherDto.getTeacherName() == null){
            XueChengException.cast(CommonError.PARAMS_ERROR);
        }
        if (courseTeacherDto.getPosition() == null){
            XueChengException.cast(CommonError.PARAMS_ERROR);
        }
        CourseTeacher courseTeacher;
        if (courseTeacherDto.getId() == null){ //新增
            courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
            courseTeacher.setCreateDate(LocalDateTime.now());
            this.baseMapper.insert(courseTeacher);
        }else { // 更新
            courseTeacher = this.baseMapper.selectById(courseTeacherDto.getId());
            if (courseTeacher!=null){
                courseTeacherDto.setCreateDate(courseTeacher.getCreateDate());
                BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
                this.baseMapper.updateById(courseTeacher);
            }else {
                courseTeacher = new CourseTeacher();
                BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
                courseTeacher.setCreateDate(LocalDateTime.now());
                this.baseMapper.insert(courseTeacher);
            }
        }
        CourseTeacherDto courseTeacherDtoReturn = new CourseTeacherDto();
        BeanUtils.copyProperties(courseTeacher,courseTeacherDtoReturn);
        return courseTeacherDtoReturn;
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId,courseId);
        queryWrapper.eq(CourseTeacher::getId,teacherId);
        this.baseMapper.delete(queryWrapper);
    }
}
