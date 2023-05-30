package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.xuecheng.base.execption.XueChengException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Resource
    CourseMarketMapper courseMarketMapper;

    @Resource
    CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    CourseBaseMapper courseBaseMapper;
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //获取课程基本信息，营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //获取课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);

        CoursePreviewDto previewDto = new CoursePreviewDto();
        previewDto.setCourseBase(courseBaseInfo);
        previewDto.setTeachplans(teachplanTree);
        return previewDto;
    }

    @Override
    public void commitAudit(Long companyId, Long courseId) {

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //约束校验
        //1、对已提交审核的课程不允许提交审核。
        String auditStatus = courseBase.getAuditStatus();
        if (auditStatus.equals("202003")){
            XueChengException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //2、本机构只允许提交本机构的课程。
        Long courseCompanyId = courseBase.getCompanyId();
        if (!courseCompanyId.equals(companyId)){
            XueChengException.cast("不允许提交其它机构的课程。");
        }
        //3、没有上传图片不允许提交审核。
        String pic = courseBase.getPic();
        if (StringUtils.isEmpty(pic)){
            XueChengException.cast("提交失败，请上传课程图片");
        }
        //4、没有添加课程计划不允许提交审核。
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size()<=0){
            XueChengException.cast("没有添加课程计划不允许提交审核");
        }

        //1、查询课程基本信息、课程营销信息、课程计划信息等课程相关信息，整合为课程预发布信息。
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);

        //2、向课程预发布表course_publish_pre插入一条记录，如果已经存在则更新，审核状态为：已提交。
        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCreateDate(LocalDateTime.now());
        coursePublishPre.setCompanyId(companyId);
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreUpdate == null){
            //添加课程预发布记录
            // Field 'mt_name' doesn't have a default value
            coursePublishPreMapper.insert(coursePublishPre);
        }else {
            //更新课程预发布记录
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //3、更新课程基本表course_base课程审核状态为：已提交。
        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);

    }
}
