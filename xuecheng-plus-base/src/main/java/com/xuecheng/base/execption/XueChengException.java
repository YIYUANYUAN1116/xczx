package com.xuecheng.base.execption;

/**
 * 统一异常处理
 */
public class XueChengException extends RuntimeException {
    private String errMessage;

    public XueChengException() {
        super();
    }

    public XueChengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(CommonError commonError){
        throw new XueChengException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengException(errMessage);
    }

}
