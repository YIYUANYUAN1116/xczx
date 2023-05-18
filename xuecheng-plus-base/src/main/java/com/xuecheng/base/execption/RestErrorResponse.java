package com.xuecheng.base.execption;

import java.io.Serializable;

/**
 * 错误响应参数包装
 */
public class RestErrorResponse implements Serializable {

    private String errMessage;

    private int errCode;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public RestErrorResponse(int errCode){
        this.errCode= errCode;
    }

    public RestErrorResponse(String errMessage,int errCode){
        this.errMessage= errMessage;
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
}

