package com.xuecheng.base.execption;

/**
 * 统一异常处理
 */
public class XueChengException extends RuntimeException {
    private String errMessage;
    private Long errCode;

    public XueChengException() {
        super();
    }

    public XueChengException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public XueChengException(String errMessage,Long errCode) {
        super(errMessage);
        this.errMessage = errMessage;
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public Long getErrCode() {
        return errCode;
    }

    public static void cast(CommonError commonError){
        throw new XueChengException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengException(errMessage);
    }

}
