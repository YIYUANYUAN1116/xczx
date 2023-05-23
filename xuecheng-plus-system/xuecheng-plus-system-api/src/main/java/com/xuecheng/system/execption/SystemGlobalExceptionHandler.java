package com.xuecheng.system.execption;


import com.xuecheng.base.execption.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class SystemGlobalExceptionHandler extends GlobalExceptionHandler {

}
