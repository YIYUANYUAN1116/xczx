package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
