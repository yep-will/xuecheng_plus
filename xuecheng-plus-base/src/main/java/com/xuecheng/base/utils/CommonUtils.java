package com.xuecheng.base.utils;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;

/**
 * @author will
 * @version 公共工具类
 * @description
 * @date 2023/2/17 12:57
 */
public class CommonUtils {


    /**
     * @param extension 文件扩展名
     * @return java.lang.String
     * @description 根据扩展名获取匹配的资源的媒体类型
     * @author will
     * @date 2023/2/17 12:59
     */
    public String getMimeTypeByExtension(String extension) {
        //资源的媒体类型(默认为未知的二进制流)
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (StringUtils.isNotEmpty(extension)) {
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (null != extensionMatch) {
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;
    }
}
