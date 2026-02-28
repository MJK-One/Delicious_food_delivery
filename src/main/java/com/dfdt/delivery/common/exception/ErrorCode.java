package com.dfdt.delivery.common.exception;

public interface ErrorCode {
    int getStatus();
    String getErrorCode();
    String getMessage();
}