package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;


/**
 * @author will
 * @version 1.0
 * @description 我的课程表service接口
 * @date 2023/3/13 19:59
 */
public interface MyCourseTablesService {

    /**
     * @param userId   用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto 拓展了学习资格属性
     * @description 添加选课
     * @author will
     * @date 2023/3/13 19:59
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);


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
    XcCourseTablesDto getLearnStatus(String userId, Long courseId);


    /**
     * @param chooseCourseId 选课id
     * @return boolean
     * @description 保存选课成功状态
     * @author will
     * @date 2023/3/21 23:54
     */
    boolean saveChooseCourseStatus(String chooseCourseId);


    /**
     * @param params 我的课程查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @description 查询我的课程表
     * @author will
     * @date 2023/3/22 20:27
     */
    PageResult<XcCourseTables> myCourseTables(MyCourseTableParams params);

}
