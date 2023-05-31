package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.XueChengException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    private static final String COURSE_PUBLISH = "course_publish";
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

    @Resource
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

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

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {

        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        //约束
        if(coursePublishPre == null){
            XueChengException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //1.本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengException.cast("不允许提交其它机构的课程。");
        }

        //2.课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //3.审核通过方可发布
        if(!"202004".equals(auditStatus)){
            XueChengException.cast("操作失败，课程审核通过方可发布。");
        }


        //课程发布操作对数据库操作如下：
        //1、向课程发布表course_publish插入一条记录,记录来源于课程预发布表，如果存在则更新，发布状态为：已发布。
        //2、更新course_base表的课程发布状态为：已发布
        //3、删除课程预发布表的对应记录。
        //4、向mq_message消息表插入一条消息，消息类型为：course_publish

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);

    }

    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengException.cast("课程静态化异常");
        }

        return htmlFile;

    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String courseIdString = String.valueOf(courseId);
        UploadFileResultDto course = mediaServiceClient.upload(multipartFile, "course",courseIdString+".html");
        if(course==null){
            XueChengException.cast("上传静态文件异常");
        }

    }

    /**
     * 保存课程发布信息
     * @param courseId
     */
    private void saveCoursePublish(Long courseId) {
        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null){
            //插入
            coursePublishMapper.insert(coursePublish);
        }else {
            //更新
            coursePublishMapper.updateById(coursePublish);
        }

        //跟新基本课程表
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    /**
     * 保存消息表
     * @param courseId
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage(COURSE_PUBLISH, String.valueOf(courseId), null, null);
        if (mqMessage == null){
            XueChengException.cast(CommonError.UNKOWN_ERROR);
        }
    }
}
