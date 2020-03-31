package com.taoyuanx.littlefile.combine.ex;

public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 8793672380339632040L;
    private Integer errorCode;

    public ServiceException(String msg) {
        super(msg);
        this.errorCode = 500;
    }

    public ServiceException(Integer errorCode, String msg) {
        super(msg);
        if (errorCode != null) {
            this.errorCode = errorCode;
        }

    }

    public Integer getErrorCode() {
        return errorCode;
    }

}
