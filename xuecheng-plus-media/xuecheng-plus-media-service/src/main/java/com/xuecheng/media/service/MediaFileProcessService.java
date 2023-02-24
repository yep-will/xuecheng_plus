package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 媒资文件后台处理业务接口
 * @date 2023/2/23 12:35
 */
public interface MediaFileProcessService {

    /**
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @description 获取待处理任务
     * @author Mr.M
     * @date 2022/9/14 14:49
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /**
     * @param taskId   任务id
     * @param status   任务状态 1:未处理，2：处理成功  3处理失败
     * @param fileId   文件id
     * @param url      url
     * @param errorMsg 错误信息
     * @return void
     * @description 保存任务结果（处理成功更新状态，文件表中的url，历史任务表，删除待处理表）
     * @author will
     * @date 2023/2/23 12:44
     */
    void saveProcessFinishStatus(Long taskId,
                                 String status,
                                 String fileId,
                                 String url,
                                 String errorMsg);

}
