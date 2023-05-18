package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.execption.RestErrorResponse;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

public interface TeachplanService extends IService<Teachplan> {

    /**
     * 查询课程计划树
     * @param courseId
     * @return
     */
    public List<TeachplanDto> findTeachplanTree(long courseId);


    /**
     * @description 查询课程计划树型结构
     * @param courseId  课程id
     * @return List<TeachplanDto>
     */
    public List<TeachplanDto> findTeachplanTreeV2(long courseId);

    /**
     * 保存课程计划（添加章节，小结）
     * @param saveTeachplanDto
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 根据 teachplanId 删除 teachplanId
     * 删除第一级别的大章节时要求大章节下边没有小章节时方可删除。
     * 删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
     * @param teachplanId
     */
    RestErrorResponse deleteTeachplanById(Long teachplanId);
}
