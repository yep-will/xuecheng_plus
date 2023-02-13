package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程教师编辑接口实现类
 * @date 2023/2/13 16:50
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;


    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.po.CourseTeacher>
     * @description 获取课程教师列表
     * @author will
     * @date 2023/2/13 17:21
     */
    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }


    /**
     * @param companyId 机构id
     * @param teacher   课程教师信息
     * @return void
     * @description 新增/修改课程教师
     * @author will
     * @date 2023/2/13 19:54
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacher teacher) {
        //获取相应课程教师信息
        CourseTeacher courseTeacher = courseTeacherMapper.selectById(teacher.getId());
        if (null == courseTeacher) {
            //新增课程教师
            courseTeacher = new CourseTeacher();
            BeanUtils.copyProperties(teacher, courseTeacher);
            courseTeacher.setCreateDate(LocalDateTime.now());
            int flag = courseTeacherMapper.insert(courseTeacher);
            if (flag <= 0) {
                XueChengPlusException.cast("添加教师失败");
            }
        } else {
            //修改课程教师信息
            //获取相应的课程信息
            CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
            if (null == courseBase) {
                XueChengPlusException.cast("课程信息不存在");
            }

            //业务规则校验，本机构只允许修改本机构的课程
            if (!companyId.equals(courseBase.getCompanyId())) {
                XueChengPlusException.cast("只允许修改本机构的课程教师");
            }
            BeanUtils.copyProperties(teacher, courseTeacher);
            courseTeacher.setCreateDate(LocalDateTime.now());
            int flag = courseTeacherMapper.updateById(courseTeacher);
            if (flag <= 0) {
                XueChengPlusException.cast("编辑教师失败");
            }
        }

        return courseTeacher;
    }


    /**
     * @param courseId  课程id
     * @param teacherId 教师id
     * @return void
     * @description TODO
     * @author will
     * @date 2023/2/13 21:11
     */
    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        int flag = courseTeacherMapper.delete(queryWrapper);
        if (flag <= 0) {
            XueChengPlusException.cast("删除失败");
        }
    }


}
