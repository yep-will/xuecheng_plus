package com.xuecheng.base.exception;


/**
 * @author will
 * @version 1.0
 * @description 通用错误信息
 * @date 2023/2/11 11:23
 */
public enum CommonError {
    /**
     * 执行过程异常，请重试。
     */
    UNKNOWN_ERROR("执行过程异常，请重试。"),
    /**
     * 非法参数
     */
    PARAMS_ERROR("非法参数"),
    /**
     * 对象为空
     */
    OBJECT_NULL("对象为空"),
    /**
     * 查询结果为空
     */
    QUERY_NULL("查询结果为空"),
    /**
     * 请求参数为空
     */
    REQUEST_NULL("请求参数为空");

    private String errMessage;

    public String getErrMessage() {
        return errMessage;
    }

    private CommonError(String errMessage) {
        this.errMessage = errMessage;
    }

}
