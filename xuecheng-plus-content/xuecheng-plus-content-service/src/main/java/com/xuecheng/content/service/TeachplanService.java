package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.base.execption.RestErrorResponse;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;

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

    /**
     * 根据  teachplanId 和 moveType 移动课程计划
     * 向上移动后和上边同级的课程计划交换位置，可以将两个课程计划的排序字段值进行交换。
     * 向下移动后和下边同级的课程计划交换位置，可以将两个课程计划的排序字段值进行交换。
     * @param teachplanId
     * @param moveType
     * @return
     */
    RestErrorResponse moveTeachplanById(Long teachplanId, String moveType);


    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return com.xuecheng.content.model.po.TeachplanMedia
     * @author Mr.M
     * @date 2022/9/14 22:20
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    void deleteAssociationMedia(Long teachPlanId, String mediaId);
}
