package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author will
 * @version 1.0
 * @description 全局异常处理类
 * @date 2023/2/8 11:26
 */
@Slf4j
@ControllerAdvice//控制器增强
public class GlobalExceptionHandler {

    /**
     * @param e Exception类型异常
     * @return com.xuecheng.base.exception.RestErrorResponse
     * @description 捕获不可预知异常 Exception
     * @author will
     * @date 2023/3/11 11:10
     */
    @ResponseBody//将信息返回为 json格式
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//状态码返回500
    public RestErrorResponse doException(Exception e) {

        log.error("捕获异常：{}", e.getMessage());
        e.printStackTrace();
        if (e.getMessage().equals("不允许访问")) {
            return new RestErrorResponse("没有权限操作此功能");
        }
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }


    /**
     * @param e 拦截的异常对象
     * @return com.xuecheng.base.exception.RestErrorResponse
     * @description 处理XueChengPlusException异常, 此类异常是程序员主动抛出的可预知异常
     * @author will
     * @date 2023/2/11 12:51
     */
    @ResponseBody    //将信息返回为json格式
    @ExceptionHandler(XueChengPlusException.class)    //此方法捕获XueChengPlusException类异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)    //状态码返回500
    public RestErrorResponse doXueChengPlusException(XueChengPlusException e) {
        log.error("捕获异常：{}", e.getErrMessage());
        e.printStackTrace();
        String errMessage = e.getErrMessage();
        return new RestErrorResponse(errMessage);
    }


    /**
     * @param e 拦截的异常对象
     * @return com.xuecheng.base.exception.RestErrorResponse
     * @description 捕获MethodArgumentNotValidException异常, 处理JSR303校验出错异常
     * @author will
     * @date 2023/2/11 12:55
     */
    @ResponseBody    //将信息返回为json格式
    @ExceptionHandler(MethodArgumentNotValidException.class)  //此方法捕获MethodArgumentNotValidException异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)    //状态码返回500
    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        //校验的错误信息集
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        //收集错误信息
        StringBuffer errors = new StringBuffer();
        fieldErrors.forEach(error -> {
            errors.append(error.getDefaultMessage()).append(",");
        });

        return new RestErrorResponse(errors.toString());
    }

}
