package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.execption.RestErrorResponse;
import com.xuecheng.base.execption.XueChengException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper,Teachplan> implements TeachplanService {

    @Resource
    TeachplanMediaMapper teachplanMediaMapper;

    @Resource
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        List<Teachplan> teachplans = baseMapper.selectList(queryWrapper);
        if (teachplans == null || teachplans.size()==0){
            XueChengException.cast("课程计划不存在");
        }

        List<TeachplanDto> collect = teachplans.stream().filter((teachplan -> {
            return teachplan.getParentid() == 0;
        })).map(plan -> {
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtils.copyProperties(plan,teachplanDto);
            TeachplanMedia teachplanMedia = findTeachplanMediaByTeachplanId(plan.getId());
            if (teachplanMedia != null) {
                teachplanDto.setTeachplanMedia(teachplanMedia);
            }
            teachplanDto.setTeachPlanTreeNodes(findTeachplanChildrenTreeNodes(plan, teachplans));
            return teachplanDto;
        }).collect(Collectors.toList());
        return collect;
    }

    @Override
    public List<TeachplanDto> findTeachplanTreeV2(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        //课程计划id
        Long id = teachplanDto.getId();
        //修改课程计划
        if (id != null) {
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }else {
            //新增课程计划

            Teachplan teachplan = new Teachplan();
            //取出同父同级别的课程计划数量
            int count = getTeachplanCount(teachplanDto.getCourseId(),teachplanDto.getParentid());
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplan.setCreateDate(LocalDateTime.now());
            teachplan.setOrderby(count+1);
            teachplanMapper.insert(teachplan);
        }

    }

    @Override
    public RestErrorResponse deleteTeachplanById(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Long parentid = teachplan.getParentid();
        if (parentid == 0){ //章节
            //查询该章节下是否还有小节
            int count = findChildCountByTeachplanId(teachplan.getId());
            if (count!=0){
                return new RestErrorResponse("课程计划信息还有子级信息，无法操作",120409);
            }else {
                teachplanMapper.deleteById(teachplan.getId());
                return new RestErrorResponse(200);
            }
        }else {//小节
            teachplanMapper.deleteById(teachplan.getId());
            //删除关联的 teachplanMedia
            deleteTeachplanMediaByTeachplanId(teachplanId);
            return new RestErrorResponse(200);
        }
    }


    /**
     * 删除关联的 teachplanMedia
     * @param teachplanId
     */
    private void deleteTeachplanMediaByTeachplanId(Long teachplanId) {
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
    }

    /**
     *  查询该章节下是否还有小节
     * @param teachplanId
     * @return
     */
    private int findChildCountByTeachplanId(Long teachplanId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,teachplanId);
        return teachplanMapper.selectCount(queryWrapper);
    }

    /**
     *  查询同父同级别的课程计划数量
     * @param courseId
     * @param parentId
     * @return
     */
    private int getTeachplanCount(Long courseId,Long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        queryWrapper.eq(Teachplan::getParentid,parentId);
        return teachplanMapper.selectCount(queryWrapper);
    }

    public List<TeachplanDto> findTeachplanChildrenTreeNodes(Teachplan root,List<Teachplan> teachplans){
        List<TeachplanDto> collect = teachplans.stream().filter(teachplan -> {
            return Objects.equals(teachplan.getParentid(), root.getId());
        }).map(plan -> {
            TeachplanDto teachplanDto = new TeachplanDto();
            BeanUtils.copyProperties(plan,teachplanDto);
            TeachplanMedia teachplanMedia = findTeachplanMediaByTeachplanId(plan.getId());
            if (teachplanMedia != null) {
                teachplanDto.setTeachplanMedia(teachplanMedia);
            }
            teachplanDto.setTeachPlanTreeNodes(findTeachplanChildrenTreeNodes(plan, teachplans));
            return teachplanDto;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * 根据 teachplanId 查询 TeachplanMedia
     * @param teachplanId
     * @return
     */
    public TeachplanMedia findTeachplanMediaByTeachplanId(Long teachplanId){
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
        return teachplanMediaMapper.selectOne(queryWrapper);
    }

}
