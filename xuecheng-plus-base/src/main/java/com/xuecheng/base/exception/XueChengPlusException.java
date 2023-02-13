package com.xuecheng.base.exception;

/**
 * @author will
 * @version 1.0
 * @description 统一异常处理接口
 * @date 2023/2/8 10:53
 */
public class XueChengPlusException extends RuntimeException {

    private static final long serialVersionUID = 5565760508056698922L;

    /**
     * 承载异常错误信息
     */
    private String errMessage;

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError) {
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }

}
