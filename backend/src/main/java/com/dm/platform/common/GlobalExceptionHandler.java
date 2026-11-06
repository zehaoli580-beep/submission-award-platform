package com.dm.platform.common;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理: Controller/Service 抛出的异常在这里统一兜底,
 * 避免把原始堆栈直接返回给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 -> 带具体提示返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /** 上传文件超限 -> 提示大小上限(对应报告里的文件上传设计) */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return Result.error("文件过大, 单文件上限 200MB");
    }

    /** @Valid 校验失败 -> 取第一条错误信息返回 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? "参数不合法"
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return Result.error(msg);
    }

    /** 其他未预料异常 -> 统一模糊提示, 具体原因看后端日志 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        e.printStackTrace();
        return Result.error("系统繁忙, 请稍后重试");
    }
}
