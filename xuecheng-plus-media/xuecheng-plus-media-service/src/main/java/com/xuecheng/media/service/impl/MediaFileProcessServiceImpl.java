package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
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
 * @description 媒资文件后台处理业务接口实现类
 * @date 2023/2/23 12:37
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;


    /**
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count      获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @description 获取待处理任务
     * @author Mr.M
     * @date 2022/9/14 14:49
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }


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
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveProcessFinishStatus(Long taskId,
                                        String status,
                                        String fileId,
                                        String url,
                                        String errorMsg) {
        //查询这个任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (null == mediaProcess) {
            log.debug("更新任务状态时此任务:{}为空", taskId);
            return;
        }

        //定位记录表
        LambdaQueryWrapper<MediaProcess> queryWrapperById =
                new LambdaQueryWrapper<MediaProcess>().eq(MediaProcess::getId, taskId);

        //处理失败
        if ("3".equals(status)) {
            MediaProcess failMediaProcess = new MediaProcess();
            failMediaProcess.setStatus("3");
            failMediaProcess.setErrormsg(errorMsg);
            failMediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.update(failMediaProcess, queryWrapperById);

            return;
        }

        //处理成功，更新状态
        if ("2".equals(status)) {
            mediaProcess.setStatus("2");
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcessMapper.updateById(mediaProcess);

            //更新文件表中的url字段
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            mediaFiles.setUrl(url);
            String originalFileName = mediaFiles.getFilename();

            String[] parts = originalFileName.split("\\.");
            if (null != parts[0]) {
                mediaFiles.setFilename(parts[0] + ".mp4");
            }
            mediaFilesMapper.updateById(mediaFiles);

            //处理成功将任务添加到历史记录表
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
            if (null != parts[0]) {
                mediaProcessHistory.setFilename(parts[0] + ".mp4");
            }
            mediaProcessHistoryMapper.insert(mediaProcessHistory);

            //处理成功将待处理表的记录删除
            mediaProcessMapper.deleteById(taskId);
        }
    }

}
