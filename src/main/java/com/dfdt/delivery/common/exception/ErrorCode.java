package com.dfdt.delivery.common.exception;

public interface ErrorCode {
    public int getStatus();
    public String getErrorCode();
    public String getMessage();
}