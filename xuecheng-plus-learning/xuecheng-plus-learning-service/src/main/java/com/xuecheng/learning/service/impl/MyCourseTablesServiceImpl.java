package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
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
 * @description 我的课程表服务接口实现类
 * @date 2023/3/13 20:05
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    XcCourseTablesMapper courseTablesMapper;

    @Autowired
    MyCourseTablesServiceImpl currentProxy;


    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto 拓展了学习资格属性
     * @description 添加选课
     * @author will
     * @date 2023/3/13 19:59
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //远程调用内容管理查询课程的收费规则
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);
        if (null == coursePublish) {
            XueChengPlusException.cast("课程信息不存在");
        }

        Long id = coursePublish.getId();
        if (id == null) {
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }

        //收费规则操作
        String charge = coursePublish.getCharge();
        XcChooseCourse xcChooseCourse = null;
        if ("201000".equals(charge)) {
            //添加免费课程，向我的课程表，选课记录表插入数据（两张表）
            xcChooseCourse = currentProxy.addFreeCourse(userId, coursePublish);
        } else {
            //添加收费课程，向选课记录表插入数据（一张表）
            xcChooseCourse = currentProxy.addChargeCourse(userId, coursePublish);
        }

        //构造返回值
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);

        //获取学生的学习资格并返回
        XcCourseTablesDto xcCourseTablesDto = currentProxy.getLearnStatus(userId, courseId);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());

        return xcChooseCourseDto;
    }


    /**
     * @param userId        用户id
     * @param coursePublish 课程发布信息
     * @return com.xuecheng.learning.model.po.XcChooseCourse
     * @description 添加免费课程, 免费课程加入“选课记录表”、“我的课程表”
     * @author will
     * @date 2023/3/13 20:13
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public XcChooseCourse addFreeCourse(String userId, CoursePublish coursePublish) {
        //判断：查询选课记录表是否存在同一用户相同课程免费的且选课成功的订单，如果存在直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                //免费课程"700001"
                .eq(XcChooseCourse::getOrderType, "700001")
                //选课成功"701001"
                .eq(XcChooseCourse::getStatus, "701001");
        //数据库设置只有主键没有其它约束，有潜在的存在多条记录的风险
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size() > 0) {
            return xcChooseCourses.get(0);
        }

        //向“选课记录表”添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        //免费课程"700001"
        xcChooseCourse.setOrderType("700001");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        //收费为0
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        //免费课程默认365
        xcChooseCourse.setValidDays(365);
        //选课成功"701001"
        xcChooseCourse.setStatus("701001");
        //有效期开始时间
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        //有效期结束时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        int insertChooseCourse = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insertChooseCourse <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }

        //免费课程添加到“我的课程表”
        addCourseTables(xcChooseCourse);

        return xcChooseCourse;
    }


    /**
     * @param userId        用户id
     * @param coursePublish 课程发布信息
     * @return com.xuecheng.learning.model.po.XcChooseCourse
     * @description 添加收费课程，加入“选课记录表”
     * @author will
     * @date 2023/3/13 20:14
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public XcChooseCourse addChargeCourse(String userId, CoursePublish coursePublish) {
        //判断：查询选课记录表是否存在同一用户相同课程是收费且状态为未支付，如果存在直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursePublish.getId())
                //收费课程"700002"
                .eq(XcChooseCourse::getOrderType, "700002")
                //待支付"701002"
                .eq(XcChooseCourse::getStatus, "701002");
        //数据库设置只有主键没有其它约束，有潜在的存在多条记录的风险
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size() > 0) {
            return xcChooseCourses.get(0);
        }

        //向“选课记录表”添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursePublish.getId());
        xcChooseCourse.setCourseName(coursePublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursePublish.getCompanyId());
        //收费课程"700002"
        xcChooseCourse.setOrderType("700002");
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        //收费为0
        xcChooseCourse.setCoursePrice(coursePublish.getPrice());
        //免费课程默认365
        xcChooseCourse.setValidDays(365);
        //待支付"701002"
        xcChooseCourse.setStatus("701002");
        //有效期开始时间
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        //有效期结束时间
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));

        //添加到“选课记录表”
        int insertChooseCourse = xcChooseCourseMapper.insert(xcChooseCourse);
        if (insertChooseCourse <= 0) {
            XueChengPlusException.cast("添加选课记录失败");
        }

        return xcChooseCourse;
    }


    /**
     * @param xcChooseCourse 选课记录表课程信息
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @description 添加到“我的课程表”
     * @author will
     * @date 2023/3/13 20:16
     */
    @Transactional(rollbackFor = RuntimeException.class)
    public XcCourseTables addCourseTables(XcChooseCourse xcChooseCourse) {
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!"701001".equals(status)) {
            XueChengPlusException.cast("选课失败，无法添加到课程表");
        }
        //查询我的课程表中是否存在课程
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        if (xcCourseTables != null) {
            return xcCourseTables;
        }

        //将选课记录表课程信息封装成我的课程表信息
        xcCourseTables = new XcCourseTables();
        BeanUtils.copyProperties(xcChooseCourse, xcCourseTables);
        //选课记录表的主键
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        //选课收费类型
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insertCourseTable = xcCourseTablesMapper.insert(xcCourseTables);
        if (insertCourseTable <= 0) {
            XueChengPlusException.cast("添加我的课程表失败");
        }

        return xcCourseTables;
    }


    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.po.XcCourseTables
     * @description 根据课程id和用户id查询我的课程表中是否存在相应课程
     * @author Mr.M
     * @date 2022/10/2 17:07
     */
    public XcCourseTables getXcCourseTables(String userId, Long courseId) {
        XcCourseTables xcCourseTables =
                xcCourseTablesMapper.selectOne(new LambdaQueryWrapper<XcCourseTables>()
                        .eq(XcCourseTables::getUserId, userId)
                        .eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;
    }


    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcCourseTablesDto
     * 学习资格状态 [{"code":"702001","desc":"正常学习"},
     * {"code":"702002","desc":"没有选课或选课后没有支付"},
     * {"code":"702003","desc":"已过期需要申请续期或重新支付"}]
     * @description 获取用户-课程的学习资格
     * @author will
     * @date 2023/3/13 20:41
     */
    @Override
    public XcCourseTablesDto getLearnStatus(String userId, Long courseId) {
        //返回结果
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();

        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        if (xcCourseTables == null) {
            //{"code":"702002","desc":"没有选课或选课后没有支付"}
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }

        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        //判读是否过期,true过期，false未过期
        //boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        boolean isExpires = LocalDateTime.now().isBefore(xcCourseTables.getValidtimeEnd());
        if (isExpires) {
            //{"code":"702001","desc":"正常学习"}
            xcCourseTablesDto.setLearnStatus("702001");
            return xcCourseTablesDto;
        } else {
            //{"code":"702003","desc":"已过期需要申请续期或重新支付"}
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
    }


    /**
     * @param chooseCourseId 选课id
     * @return boolean
     * @description 保存选课成功状态
     * @author will
     * @date 2023/3/21 23:54
     */
    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public boolean saveChooseCourseStatus(String chooseCourseId) {
        //根据选课id查询选课表
        XcChooseCourse chooseCourse = xcChooseCourseMapper.selectById(chooseCourseId);
        if (chooseCourse == null) {
            log.debug("接收购买课程的信息，根据选课id从数据库找不到选课记录，选课id：{}", chooseCourseId);
            return false;
        }

        //选课状态
        String status = chooseCourse.getStatus();
        //只有当未支付时才更新为已支付
        if ("701002".equals(status)) {
            //1.更新选课记录的状态为支付成功
            chooseCourse.setStatus("701001");
            int i = xcChooseCourseMapper.updateById(chooseCourse);
            if (i <= 0) {
                log.debug("添加选课记录失败：{}", chooseCourse);
                XueChengPlusException.cast("添加选课记录失败");
            }
            //2.向我的课程表插入记录
            addCourseTables(chooseCourse);

            return true;
        }
        return false;
    }


    /**
     * @param params 我的课程查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @description 查询我的课程表
     * @author will
     * @date 2023/3/22 20:27
     */
    @Override
    public PageResult<XcCourseTables> myCourseTables(MyCourseTableParams params) {
        //当前页码
        long pageNo = params.getPage();
        //每页记录数
        long pageSize = params.getSize();
        //构造分页条件
        Page<XcCourseTables> page = new Page<>(pageNo, pageSize);
        //获取用户id
        String userId = params.getUserId();

        //构造查询条件
        LambdaQueryWrapper<XcCourseTables> lambdaQueryWrapper = new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId);
        //进行分页查询
        Page<XcCourseTables> pageResult = courseTablesMapper.selectPage(page, lambdaQueryWrapper);

        //获取数据列表
        List<XcCourseTables> records = pageResult.getRecords();
        //数据列表总数
        long total = pageResult.getTotal();

        //封装返回参数
        PageResult<XcCourseTables> courseTablesResult = new PageResult<>(records, total, pageNo, pageSize);
        return courseTablesResult;
    }

}
