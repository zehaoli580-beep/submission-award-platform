package com.dm.platform.common;

/**
 * 业务异常: 所有"能预见的业务错误"(重名、非法状态流转、文件类型不对...)
 * 都抛它, 由全局异常处理器统一转成友好的接口返回
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
