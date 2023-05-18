package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    CourseBaseMapper courseBaseMapper;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase>    queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        if (queryCourseParamsDto != null){
            queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
            //构建查询条件，根据课程审核状态查询
            queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
            //构建查询条件，根据课程发布状态查询
            //todo:根据课程发布状态查询
            queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        }
        //分页查询参数
        Page<CourseBase> courseBasePage = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<CourseBase> selectPage = courseBaseMapper.selectPage(courseBasePage, queryWrapper);

        return new PageResult<>(selectPage.getRecords(),selectPage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        //合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
            throw new XueChengException("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            throw new XueChengException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            throw new XueChengException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            throw new XueChengException("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            throw new XueChengException("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            throw new XueChengException("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            throw new XueChengException("收费规则为空");
        }

        //1.保存课程信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        //设置companyId
        courseBase.setCompanyId(companyId);
        //设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //设置审核状态
        courseBase.setAuditStatus("202002");
        //设置发布状态
        courseBase.setStatus("203001");

        int courseBaseInsert = courseBaseMapper.insert(courseBase);
        if(courseBaseInsert<=0){
            throw new XueChengException("新增课程基本信息失败---courseBaseInsert");
        }

        //2.向课程营销表保存课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        courseMarket.setId(courseBase.getId());
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        saveCourseMarket(courseMarket);


        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseBase.getId());

    }

    private void saveCourseMarket(CourseMarket courseMarket) {
        //收费规则
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new XueChengException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() <=0){
                throw new XueChengException("课程为收费价格不能为空且必须大于0");
            }
        }

        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarket.getId());
        int courseMarketInsert = -1;
        if (courseMarketObj == null){
            courseMarketInsert = courseMarketMapper.insert(courseMarket);
        }else {
            BeanUtils.copyProperties(courseMarket,courseMarketObj);
            courseMarketObj.setId(courseMarket.getId());
            courseMarketInsert= courseMarketMapper.updateById(courseMarketObj);
        }

        if(courseMarketInsert<=0){
            throw new XueChengException("新增课程基本信息失败---courseMarketInsert");
        }
    }

    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(courseBase.getMt());
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBase.getSt());

        courseBaseInfoDto.setStName(courseCategorySt.getName());
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());
        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {

        //课程id
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase==null){
            XueChengException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengException.cast("本机构只能修改本机构的课程");
        }

        //更新课程信息
        //1.保存课程信息
        CourseBase courseBaseUpdate = new CourseBase();
        BeanUtils.copyProperties(dto,courseBaseUpdate);
        courseBaseUpdate.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBaseUpdate);

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        saveCourseMarket(courseMarket);



        return getCourseBaseInfo(courseId);
    }
}
