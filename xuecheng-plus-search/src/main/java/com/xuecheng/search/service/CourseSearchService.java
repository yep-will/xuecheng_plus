package com.xuecheng.search.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.search.dto.SearchCourseParamDto;
import com.xuecheng.search.dto.SearchPageResultDto;
import com.xuecheng.search.po.CourseIndex;

/**
 * @author Mr.M
 * @version 1.0
 * @description 课程搜索service
 * @date 2022/9/24 22:40
 */
public interface CourseSearchService {

    /**
     * @param pageParams           分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.search.po.CourseIndex> 课程列表
     * @description 搜索课程列表
     * @author will
     * @date 2023/3/3 23:10
     */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

}
