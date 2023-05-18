package com.xuecheng.content.execption;


import com.xuecheng.base.execption.CommonError;
import com.xuecheng.base.execption.GlobalExceptionHandler;
import com.xuecheng.base.execption.RestErrorResponse;
import com.xuecheng.base.execption.XueChengException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class ContentGlobalExceptionHandler extends GlobalExceptionHandler {

}
