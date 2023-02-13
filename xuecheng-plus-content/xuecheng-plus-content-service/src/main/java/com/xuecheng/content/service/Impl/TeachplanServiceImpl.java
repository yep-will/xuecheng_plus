package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
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


    private final static String MOVEUP = "moveup";

    private final static String MOVEDOWN = "movedown";


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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTeachplan(SaveTeachplanDto dto) {
        Long id = dto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (null == teachplan) {
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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTeachplan(Long teachplanId) {
        if (null == teachplanId) {
            XueChengPlusException.cast(CommonError.OBJECT_NULL);
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
     * @param moveType    移动类型
     * @param teachplanId 课程计划id
     * @return void
     * @description 根据移动类型对课程计划排序
     * @author will
     * @date 2023/2/13 15:40
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void orderByTeachplan(String moveType, Long teachplanId) {
        //获取课程计划对象信息
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        //获取当前课程计划排序值
        Integer orderby = teachplan.getOrderby();

        if (MOVEUP.equals(moveType)) {
            //向上移动
            //获取上一个课程计划
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
            queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            queryWrapper.eq(Teachplan::getOrderby, orderby - 1);
            Teachplan upTeachplan = teachplanMapper.selectOne(queryWrapper);
            if (null == upTeachplan) {
                XueChengPlusException.cast("已经到头了");
                return;
            }
            exchangeOrderby(teachplan, upTeachplan);
        } else if (MOVEDOWN.equals(moveType)) {
            //向下移动
            //获取下一个课程计划
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
            queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
            queryWrapper.eq(Teachplan::getOrderby, orderby + 1);
            Teachplan downTeachplan = teachplanMapper.selectOne(queryWrapper);
            if (null == downTeachplan) {
                XueChengPlusException.cast("已经到底了");
                return;
            }
            exchangeOrderby(teachplan, downTeachplan);
        }
    }


    /**
     * @param teachplan1 课程计划1
     * @param teachplan2 课程计划2
     * @return void
     * @description 交换两个课程计划的orderby
     * @author will
     * @date 2023/2/13 16:16
     */
    private void exchangeOrderby(Teachplan teachplan1, Teachplan teachplan2) {
        Integer orderby1 = teachplan1.getOrderby();
        Integer orderby2 = teachplan2.getOrderby();
        teachplan1.setOrderby(orderby2);
        teachplan2.setOrderby(orderby1);
        teachplanMapper.updateById(teachplan1);
        teachplanMapper.updateById(teachplan2);
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
