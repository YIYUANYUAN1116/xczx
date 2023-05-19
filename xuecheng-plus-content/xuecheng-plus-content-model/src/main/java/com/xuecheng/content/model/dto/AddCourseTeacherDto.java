package com.xuecheng.content.model.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xuecheng.base.execption.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Data
@ApiModel(value="AddCourseDto", description="新增教师基本信息")
public class AddCourseTeacherDto {



    /**
     * 课程标识
     */
    @NotEmpty(groups = {ValidationGroups.Insert.class,ValidationGroups.Update.class},message = "课程id不能为空")
    @ApiModelProperty(value = "课程ID", required = true)
    private Long courseId;

    /**
     * 教师标识
     */
    @NotEmpty(groups = {ValidationGroups.Insert.class,ValidationGroups.Update.class},message = "教师姓名不能为空")
    @ApiModelProperty(value = "教师姓名",required = true)
    private String teacherName;

    /**
     * 教师职位
     */
    @NotEmpty(groups = {ValidationGroups.Insert.class,ValidationGroups.Update.class},message = "教师职位不能为空")
    @ApiModelProperty(value = "教师职位",required = true)
    private String position;

    /**
     * 教师简介
     */
    @ApiModelProperty(value = "教师简介")
    private String introduction;

    /**
     * 照片
     */
    @ApiModelProperty(value = "照片")
    private String photograph;
}
