package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author will
 * @version 1.0
 * @description 视频处理
 * @date 2023/2/23 20:32
 */
@Component
@Slf4j
public class VideoTask {

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    /**
     * @return void
     * @description 视频处理任务
     * @author will
     * @date 2023/2/23 20:43
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 分片序号，从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        // 分片总数
        int shardTotal = XxlJobHelper.getShardTotal();

        //查询待处理任务,一次处理的任务数和cpu核心数一样，这里暂时默认为2
        List<MediaProcess> mediaProcessList =
                mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, 2);

        if (null == mediaProcessList || mediaProcessList.size() <= 0) {
            log.debug("查询到的待处理视频任务为0");
            return;
        }

        //获取要处理的任务数
        int poolSize = mediaProcessList.size();

        // 黑马代码：ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        // 手动创建线程池 ThreadPoolExecutor 类的构造函数参数分别为：
        //corePoolSize：线程池中核心线程的数量，即在没有任务需要执行时线程池的大小。
        //maximumPoolSize：线程池中最大线程数。
        //keepAliveTime：当线程池中的线程数量大于 corePoolSize 时，多余的空闲线程的存活时间。
        //unit：存活时间的单位。
        //workQueue：等待执行的任务队列。
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, queue);

        //计数器
        //阻塞到任务执行完成,当countDownLatch计数器归零，这里的阻塞解除
        CountDownLatch countDownLatch = new CountDownLatch(poolSize);

        //遍历mediaProcessList，将任务放入线程池
        mediaProcessList.forEach(mediaProcess -> {
            threadPool.execute(() -> {
                //任务的执行逻辑
                //视频处理状态
                String status = mediaProcess.getStatus();
                //保证幂等性
                if ("2".equals(status)) {
                    log.debug("视频已经处理不用再次处理,视频信息:{}", mediaProcess);
                    //计数器减1
                    countDownLatch.countDown();
                    return;
                }
                //桶
                String bucket = mediaProcess.getBucket();
                //存储路径
                String filePath = mediaProcess.getFilePath();
                //原始视频的md5值
                String fileId = mediaProcess.getFileId();
                //原始文件名称
                String filename = mediaProcess.getFilename();

                //将要处理的文件下载到服务器上
                File originalFile = null;
                //处理结束的视频文件
                File mp4File = null;

                try {
                    originalFile = File.createTempFile("original", null);
                    // 临时创建的MP4文件只是临时的，后续会删除，所以命名不做要求
                    mp4File = File.createTempFile("mp4", ".mp4");
                } catch (IOException e) {
                    log.error("处理视频前创建临时文件失败");
                    //当前媒资进程处理失败，计数器减1
                    countDownLatch.countDown();
                    return;
                }

                try {
                    //将原始视频下载到本地
                    mediaFileService.downloadFileFromMinIO(originalFile, bucket, filePath);
                } catch (Exception e) {
                    log.error("下载源始文件过程出错:{},文件信息:{}", e.getMessage(), mediaProcess);
                    //当前媒资进程处理失败，计数器减1
                    countDownLatch.countDown();
                    return;
                }

                //调用工具类将avi转成mp4
                //源avi视频的路径
                String video_absolutePath = originalFile.getAbsolutePath();
                //转换后mp4文件的名称
                String mp4_name = fileId + ".mp4";
                //转换后mp4文件的文件路径
                String mp4_path = mp4File.getAbsolutePath();
                //创建工具类对象
                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_absolutePath, mp4_name, mp4_path);

                //开始视频转换，成功将返回success,失败返回失败原因
                String result = videoUtil.generateMp4();
                String statusResult = "3";
                //最终访问路径
                String url = null;
                if ("success".equals(result)) {
                    //转换成功
                    //上传到minio的路径
                    String objectName = getFilePath(fileId, ".mp4");
                    try {
                        //上传到minIO
                        mediaFileService.addMediaFilesToMinIO(mp4_path, bucket, objectName);
                    } catch (Exception e) {
                        log.debug("上传文件出错:{}", e.getMessage());
                        //当前媒资进程处理失败，计数器减1
                        countDownLatch.countDown();
                        return;
                    }
                    //处理成功
                    statusResult = "2";
                    url = "/" + bucket + "/" + objectName;
                }

                try {
                    //记录任务处理结果
                    mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), statusResult, fileId, url, result);
                } catch (Exception e) {
                    log.debug("保存任务处理结果出错:{}", e.getMessage());
                    //当前媒资进程处理失败，计数器减1
                    countDownLatch.countDown();
                    return;
                } finally {
                    if(originalFile.exists()){
                        originalFile.delete();
                    }
                    if(mp4File.exists()){
                        mp4File.delete();
                    }
                }
                //当前媒资进程任务成功，计数器减去1
                countDownLatch.countDown();
            });
        });

        //阻塞到任务执行完成,当countDownLatch计数器归零，这里的阻塞解除
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }


    /**
     * @param fileMd5 md5值（文件名）
     * @param fileExt 文件后缀
     * @return java.lang.String
     * @description 获取存储在minIO的路径
     * @author will
     * @date 2023/2/23 21:42
     */
    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

}
