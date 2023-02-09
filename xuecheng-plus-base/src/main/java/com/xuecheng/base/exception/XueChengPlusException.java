package com.xuecheng.base.exception;
/**
* @description 统一异常处理接口
* @author will
* @date 2023/2/8 10:53
* @version 1.0
*/
public class XueChengPlusException extends RuntimeException {

    private static final long serialVersionUID = 5565760508056698922L;

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

    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }

    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

}
