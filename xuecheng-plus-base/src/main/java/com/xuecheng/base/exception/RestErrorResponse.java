package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * @author will
 * @version 1.0
 * @description 错误响应参数包装
 * @date 2023/2/8 11:10
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
