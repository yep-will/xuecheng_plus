package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程信息管理业务接口实现类
 * @date 2023/2/6 21:42
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Autowired
    CourseMarketServiceImpl courseMarketService;


    /**
     * @param pageParams           分页参数
     * @param queryCourseParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
     * @description 课程查询接口实现方法
     * @author will
     * @date 2023/2/7 21:44
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件,根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //构建查询条件,根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //构建查询条件,根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        //查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        //获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        //获取数据总数
        long total = pageResult.getTotal();
        //构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }


    /**
     * @param companyId 教学机构id
     * @param dto       课程基本信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @description 添加课程基本信息接口, 课程信息包括基本信息、营销信息
     * @author will
     * @date 2023/2/11 17:02
     */
    @Transactional(rollbackFor=Exception.class)
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        //对课程基本信息数据进行封装, 调用mapper进行数据持久化
        CourseBase courseBase = new CourseBase();
        //将dto中和courseBase属性名一样的属性值拷贝到courseBase(dto->courseBase)
        BeanUtils.copyProperties(dto, courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");

        //向课程基本表插入一条记录(insert>0表示插入成功)
        int insert = courseBaseMapper.insert(courseBase);
        //获取课程id
        Long courseId = courseBase.getId();

        //对课程营销信息数据进行封装
        CourseMarket courseMarket = new CourseMarket();
        //将dto中和courseMarket属性名一样的属性值拷贝到courseMarket
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);

        //向课程营销表插入一条记录
        int insertMarket = this.saveCourseMarket(courseMarket);

        //检查是否添加成功
        if (insert <= 0 || insertMarket <= 0) {
            XueChengPlusException.cast("更新课程失败");
        }

        //组装要返回的结果
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }


    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto 课程信息
     * @description 根据课程id查询课程的基本和营销信息
     * @author will
     * @date 2023/2/11 17:07
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        //基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        if (null != courseBase) {
            BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        }
        if (null != courseMarket) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //根据课程分类的id查询分类的名称
        String mt = courseBase.getMt();
        String st = courseBase.getSt();

        CourseCategory mtCategory = courseCategoryMapper.selectById(mt);
        CourseCategory stCategory = courseCategoryMapper.selectById(st);
        if (null != mtCategory) {
            //分类名称
            String mtName = mtCategory.getName();
            courseBaseInfoDto.setMtName(mtName);
        }
        if (null != stCategory) {
            //分类名称
            String stName = stCategory.getName();
            courseBaseInfoDto.setStName(stName);
        }

        return courseBaseInfoDto;

    }


    /**
     * @param companyId 机构id
     * @param dto       课程基本信息
     * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
     * @description 更新课程
     * @author will
     * @date 2023/2/9 12:05
     */
    @Transactional(rollbackFor=Exception.class)
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        //课程id
        Long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (null == courseBase) {
            XueChengPlusException.cast("课程信息不存在");
        }
        //业务规则校验，本机构只允许修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("只允许修改本机构的课程");
        }

        //封装基本信息数据
        //将请求参数拷贝到待修改对象中
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        //更新到数据库
        int insertBase = courseBaseMapper.updateById(courseBase);

        //查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (null == courseMarket) {
            courseMarket = new CourseMarket();
        }

        //封装营销信息数据
        BeanUtils.copyProperties(dto, courseMarket);

        //对营销表进行更新,没有则添加
        int insertUpdate = this.saveCourseMarket(courseMarket);

        if (insertBase <= 0 || insertUpdate <= 0) {
            XueChengPlusException.cast("更新课程失败");
        }

        return getCourseBaseInfo(courseId);
    }


    /**
     * @param courseMarket 课程营销信息
     * @return int
     * @description 抽取对课程营销信息的校验以及保存功能(仅在本类中使用)
     * @author will
     * @date 2023/2/9 14:04
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            XueChengPlusException.cast("收费规则没有选择, 请设置");
        }
        if ("201001".equals(charge)) {
            Float price = courseMarket.getPrice();
            if (price == null || price.floatValue() <= 0) {
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b ? 1 : -1;
    }
}
