package com.xuecheng.content.service.Impl;


import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.feignclient.model.po.CourseIndex;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author will
 * @version 1.0
 * @description 课程预览、发布接口实现类
 * @date 2023/3/1 14:45
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseTeacherService courseTeacherService;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    RedisTemplate redisTemplate;

    //Jedis jedis = new Jedis("localhost", 6379);


    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.dto.CoursePreviewDto
     * @description 获取课程预览信息（包含基本信息，营销信息，课程计划，师资信息）
     * @author will
     * @date 2023/3/1 14:44
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //获取课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //获取课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        //获取师资信息
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(courseId);

        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplanTree);
        coursePreviewDto.setCourseTeachers(courseTeacherList);
        return coursePreviewDto;
    }


    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 提交审核
     * @author will
     * @date 2023/3/1 21:24
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //约束校验

        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)) {
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //课程图片是否填写
        if (StringUtils.isEmpty(courseBase.getPic())) {
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree.size() <= 0) {
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }

        //封装数据：基本信息，营销信息，课程计划信息，师资信息
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //1. 封装课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //2. 封装课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //将课程营销信息转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);
        //3. 封装课程计划信息
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //4. 封装课程师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacherList(courseId);
        if (null != courseTeachers) {
            String courseTeachersJson = JSON.toJSONString(courseTeachers);
            coursePublishPre.setTeachers(courseTeachersJson);
        }

        //设置预发布记录状态,已提交
        //{"code":"202001","desc":"审核未通过"},
        // {"code":"202002","desc":"未提交"},
        // {"code":"202003","desc":"已提交"},
        // {"code":"202004","desc":"审核通过"}
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //判断添加还是更新
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if (null == coursePublishPreUpdate) {
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新课程预发布记录
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }


    /**
     * @param companyId 机构id
     * @param courseId  课程id
     * @return void
     * @description 课程发布接口实现类
     * @author will
     * @date 2023/3/2 0:34
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publish(Long companyId, Long courseId) {
        //约束校验

        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (null == coursePublishPre) {
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过方可发布
        if (!"202004".equals(auditStatus)) {
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息到课程发布表
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
    }


    /**
     * @param courseId 课程id
     * @return void
     * @description 保存课程发布信息
     * @author will
     * @date 2023/3/2 0:36
     */
    private void saveCoursePublish(Long courseId) {
        //整合课程发布信息
        //课程发布信息来源于预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (null == coursePublishPre) {
            XueChengPlusException.cast("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();

        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //设置状态已发布
        coursePublish.setStatus("203002");

        //课程发布表course_publish插入一条记录，如果存在则更新，发布状态为：已发布。
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (null == coursePublishUpdate) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);
    }


    /**
     * @param courseId 课程id
     * @return void
     * @description 保存消息表记录
     * @author will
     * @date 2023/3/3 0:51
     */
    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast("添加消息记录失败");
        }
    }


    /**
     * @param courseId 课程id
     * @return java.io.File
     * @description 课程页面静态化
     * @author will
     * @date 2023/3/3 12:43
     */
    @Override
    public File generateCourseHtml(Long courseId) {
        //静态化文件
        File htmlFile = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建临时文件作为html文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}", htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
            return htmlFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (htmlFile.exists()) {
                log.info("临时文件所在目录:{}", htmlFile.getAbsolutePath());
                htmlFile.delete();
            }
        }
    }


    /**
     * @param courseId 课程id
     * @param file     静态话文件
     * @return void
     * @description 上传课程静态化页面
     * @author will
     * @date 2023/3/3 12:43
     */
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course", courseId + ".html");
        if (null == course) {
            XueChengPlusException.cast("远程调用媒资服务上传文件失败");
        }
    }


    /**
     * @param courseId 课程id
     * @return java.lang.Boolean
     * @description 向elasticsearch索引保存课程信息
     * @author will
     * @date 2023/3/3 23:53
     */
    @Override
    public Boolean saveCourseIndex(Long courseId) {
        //取出课程发布信息数据
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        //作异常处理
        if (null == coursePublish) {
            XueChengPlusException.cast("获取课程发布信息数据异常");
        }
        //拷贝至课程索引对象
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        //远程调用搜索服务api添加课程信息到索引
        Boolean result = searchServiceClient.add(courseIndex);
        if (!result) {
            XueChengPlusException.cast("创建课程索引失败");
        }
        return result;
    }


    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.po.CoursePublish
     * @description 根据课程id查询课程发布信息
     * @author will
     * @date 2023/3/12 22:15
     */
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }


    /**
     * @param courseId 课程id
     * @return com.xuecheng.content.model.po.CoursePublish
     * @description 查询缓存中的课程信息
     * @author will
     * @date 2023/3/24 16:55
     */
    @Override
    public CoursePublish getCoursePublishCache(Long courseId) {
        //查询缓存
        Object jsonObj = redisTemplate.opsForValue().get("course:" + courseId);
        //Object jsonObj = jedis.get("course:" + courseId);

        if (jsonObj != null) {
            String jsonString = jsonObj.toString();
            if (jsonString.equals("null")) {
                return null;
            }
            //从缓存查询
            System.out.println("=============从缓存查询=============");
            CoursePublish coursePublish = JSON.parseObject(jsonString, CoursePublish.class);
            return coursePublish;

        } else {
            //从数据库查询
            System.out.println("=============从数据库查询=============" + i++);
            CoursePublish coursePublish = getCoursePublish(courseId);

            if (coursePublish != null) {
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish));
                //jedis.set("course:" + courseId, JSON.toJSONString(coursePublish));
            } else {
                //缓存空值设置过期时间30秒
                redisTemplate.opsForValue().set("course:" + courseId, JSON.toJSONString(coursePublish), 30, TimeUnit.SECONDS);
            }
            return coursePublish;
        }
    }

    int i = 1;
}
