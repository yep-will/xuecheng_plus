package com.xuecheng.content.service.Impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 课程分类操作相关服务实现类
 * @date 2023/2/7 16:58
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;


    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //得到了根结点下边的所有子结点
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //定义一个List作为最终返回的数据
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = new ArrayList<>();

        //为了方便找子结点的父结点，定义一个map(key是结点id, value是结点本身)
        HashMap<String, CourseCategoryTreeDto> nodeMap = new HashMap<>();
        //将数据封装到List中，只包括了根结点的直接下属结点!
        categoryTreeDtos.stream().forEach(item->{
            nodeMap.put(item.getId(), item);    //每遍历一次都将结点放进map中
            if(item.getParentid().equals(id)){
                //当前结点是 根 结点的直接下属-放进最终list中
                courseCategoryTreeDtos.add(item);
            }
            //找到该结点的父结点id
            String parentid = item.getParentid();
            //找到该结点的父结点对象  ---这里要注意:在数据库中遍历的时候已经实现了父结点一定是在其子结点前遍历的
            CourseCategoryTreeDto parentNode = nodeMap.get(parentid);
            if(null != parentNode){
                List childrenTreeNodes = parentNode.getChildrenTreeNodes();
                if(null == childrenTreeNodes){
                    //首次进入父结点需要创建子群结点用来给存储后续孩子结点
                    parentNode.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //找到子结点，放到它的父结点的childrenTreeNodes属性中
                parentNode.getChildrenTreeNodes().add(item);
            }

        });

        //返回的list中只包括了根结点的直接下属结点
        return courseCategoryTreeDtos;
    }
}
