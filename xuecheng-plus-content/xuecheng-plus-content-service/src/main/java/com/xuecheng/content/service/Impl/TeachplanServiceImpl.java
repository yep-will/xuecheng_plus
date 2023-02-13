package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程计划相关操作接口实现类
 * @date 2023/2/9 16:52
 */
@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    /**
     * @param courseId 课程id
     * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
     * @description 查询课程相应的课程计划树型结构
     * @author will
     * @date 2023/2/9 16:51
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }


    /**
     * @param dto 课程计划信息
     * @return void
     * @description 保存课程计划(新增 / 修改)
     * @author will
     * @date 2023/2/9 21:10
     */
    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto dto) {
        Long id = dto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            teachplan = new Teachplan();
            BeanUtils.copyProperties(dto, teachplan);
            //找到同级课程计划的数量
            int count = getTeachplanCount(dto.getCourseId(), dto.getParentid());
            //新课程计划的值
            teachplan.setOrderby(count + 1);
            teachplanMapper.insert(teachplan);
        } else {
            BeanUtils.copyProperties(dto, teachplan);
            //更新
            teachplanMapper.updateById(teachplan);
        }
    }


    /**
     * @param teachplanId 课程计划id
     * @return void
     * @description 删除课程计划(包括大章 / 小节)
     * @author will
     * @date 2023/2/13 10:56
     */
    @Override
    public void deleteTeachplan(Long teachplanId) {
        if (null == teachplanId) {
            XueChengPlusException.cast("课程计划id为空");
        }

        //获取课程计划对象信息
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        //查看当前课程计划是否有子群
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, teachplanId);
        Integer childNum = teachplanMapper.selectCount(queryWrapper);

        if (childNum > 0) {
            XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
        } else {
            //当前课程计划下没有小节, 对该课程计划和对应的媒资信息进行删除
            teachplanMapper.deleteById(teachplanId);

            //删除该课程计划对应的媒资信息
            LambdaQueryWrapper<TeachplanMedia> mediaLambdaQueryWrapper = new LambdaQueryWrapper<>();
            mediaLambdaQueryWrapper.eq(TeachplanMedia::getTeachplanId, teachplanId);
            teachplanMediaMapper.delete(mediaLambdaQueryWrapper);

            //对同级的其他课程计划进行重新排序
            //获取被删除课程计划的排序字段值
            Integer orderby = teachplan.getOrderby();
            //获取被删除课程计划对应课程id
            Long courseId = teachplan.getCourseId();
            //获取被删除课程计划的父结点id
            Long parentid = teachplan.getParentid();

            //寻找该课程下的其他大章/该大章下的其他小节
            LambdaQueryWrapper<Teachplan> chapterQueryWrapper = new LambdaQueryWrapper();
            chapterQueryWrapper.eq(Teachplan::getParentid, parentid);
            chapterQueryWrapper.eq(Teachplan::getCourseId, courseId);
            List<Teachplan> teachplanList = teachplanMapper.selectList(chapterQueryWrapper);

            if (teachplanList.size() > 0) {
                for (Teachplan singleTeachplan : teachplanList) {
                    if (orderby < singleTeachplan.getOrderby()) {
                        singleTeachplan.setOrderby(singleTeachplan.getOrderby() - 1);
                        teachplanMapper.updateById(singleTeachplan);
                    }
                }
            }
        }

    }


    /**
     * @param courseId 课程id
     * @param parentId 父课程计划id
     * @return int 最新排序号
     * @description 获取最新的排序号
     * 找到同级课程计划的数量 SELECT count(1) from teachplan where course_id=117 and parentid=268
     * @author will
     * @date 2023/2/9 21:13
     */
    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        queryWrapper.eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count.intValue();
    }
}
