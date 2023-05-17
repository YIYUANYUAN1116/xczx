package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //将list转map,以备使用,排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //最终返回的list
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //依次遍历每个元素,排除根节点
        courseCategoryTreeDtos.stream().filter(item->!id.equals(item.getId())).forEach(item->{
            if(item.getParentid().equals(id)){
                categoryTreeDtos.add(item);
            }
            //找到当前节点的父节点
            CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
            if(courseCategoryTreeDto!=null){
            if(courseCategoryTreeDto.getChildrenTreeNodes() ==null){
                courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
            }
            //下边开始往ChildrenTreeNodes属性中放子节点
            courseCategoryTreeDto.getChildrenTreeNodes().add(item);
        }
    });
    return categoryTreeDtos;
    }


    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        List<CourseCategory> allCourseCategory = this.list();
        List<CourseCategoryTreeDto> collect = allCourseCategory.stream().filter(courseCategory -> {
            return courseCategory.getParentid().equals("1");
        }).map((menu) -> {
            CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
            BeanUtils.copyProperties(menu, courseCategoryTreeDto);
            if (menu.getIsLeaf().equals(1)){
                courseCategoryTreeDto.setChildrenTreeNodes(null);
            }else {
                courseCategoryTreeDto.setChildrenTreeNodes(findChildrenTreeNodes(menu,allCourseCategory));
            }
            return courseCategoryTreeDto;
        }).sorted(Comparator.comparingInt(CourseCategory::getOrderby)).collect(Collectors.toList());
        return collect;
    }

    /**
     * 递归获取课程分类
     * @return
     */
    public List<CourseCategoryTreeDto> findChildrenTreeNodes(CourseCategory root,List<CourseCategory> allCourseCategory){

        List<CourseCategoryTreeDto> collect = allCourseCategory.stream().filter(courseCategory ->
                Objects.equals(courseCategory.getParentid(), root.getId())
        ).map(courseCategory -> {
            CourseCategoryTreeDto courseCategoryTreeDto = new CourseCategoryTreeDto();
            BeanUtils.copyProperties(courseCategory, courseCategoryTreeDto);
            if (courseCategory.getIsLeaf().equals(1)){
                courseCategoryTreeDto.setChildrenTreeNodes(null);
            }else {
                courseCategoryTreeDto.setChildrenTreeNodes(findChildrenTreeNodes(courseCategory,allCourseCategory));
            }
            return courseCategoryTreeDto;

        }).sorted(Comparator.comparingInt(CourseCategory::getOrderby)).collect(Collectors.toList());
        return  collect;
    }
}
