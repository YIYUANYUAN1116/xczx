package com.xuecheng.content.jobhandle;

import com.xuecheng.base.execption.XueChengException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Resource
    CoursePublishMapper coursePublishMapper;

    @Autowired
    SearchServiceClient searchServiceClient;
    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    //任务调度入口
    @Override
    public boolean execute(MqMessage mqMessage) {
        //获取消息相关的业务信息
        String businessKey1 = mqMessage.getBusinessKey1();
        long courseId = Integer.parseInt(businessKey1);
        //课程静态化
        generateCourseHtml(mqMessage,courseId);
        //课程索引
        saveCourseIndex(mqMessage,courseId);
        //课程缓存
        saveCourseCache(mqMessage,courseId);
        return true;

    }

    /**
     * 将课程缓存
     * @param mqMessage
     * @param courseId
     */
    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        log.debug("开始进行课程缓存,课程id:{}",courseId);
        Long messageId = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(messageId);
        if(stageThree >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        try {
            //todo 课程缓存
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //保存第一阶段状态
        mqMessageService.completedStageThree(messageId);
    }

    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        log.debug("开始进行课程索引,课程id:{}",courseId);
        Long messageId = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(messageId);
        if(stageTwo >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        Boolean result = saveCourseIndex(courseId);
        if(result){
            //保存第一阶段状态
            mqMessageService.completedStageTwo(messageId);
        }
        //保存第一阶段状态
        mqMessageService.completedStageTwo(messageId);
    }

    private Boolean saveCourseIndex(long courseId) {
        //取出课程发布信息
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        //远程调用搜索服务api添加课程信息到索引
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengException.cast("添加索引失败");
        }
        return add;

    }

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long messageId = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(messageId);
        if(stageOne >0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        File generateCourseHtml = coursePublishService.generateCourseHtml(courseId);
        if (generateCourseHtml!=null){
            coursePublishService.uploadCourseHtml(courseId,generateCourseHtml);
        }

        //保存第一阶段状态
        mqMessageService.completedStageOne(messageId);
    }

}
