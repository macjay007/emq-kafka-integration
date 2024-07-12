package com.mac.websocket.exception;

/**
 * @author zj
 * @Date 2024/7/12 16:07
 **/
public class CommonException extends Exception {
    private static final long serialVersionUID = 1L;

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }
}
