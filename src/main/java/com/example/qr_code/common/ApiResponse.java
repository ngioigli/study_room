package com.example.qr_code.common;

import lombok.Data;

/**
 * 统一 API 响应类
 * 支持 success + code 双字段兼容模式
 */
@Data
public class ApiResponse<T> {
    
    /** 响应码：0=成功，非0=错误码 */
    private int code;
    
    /** 兼容旧版的 success 字段 */
    private boolean success;
    
    /** 响应消息 */
    private String message;
    
    /** 响应数据 */
    private T data;
    
    /** 服务器时间戳（用于幂等性校验） */
    private Long timestamp;
    
    private ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setSuccess(true);
        response.setMessage("success");
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应（带消息）
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应（只有消息）
     */
    public static <T> ApiResponse<T> successMessage(String message) {
        return success(null, message);
    }
    
    /**
     * 错误响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 错误响应（默认错误码 500）
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(500, message);
    }
    
    /**
     * 未登录响应
     */
    public static <T> ApiResponse<T> unauthorized() {
        return error(401, "请先登录");
    }
    
    /**
     * 未登录响应（自定义消息）
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }
    
    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }
    
    /**
     * 资源未找到响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }
    
    /**
     * 服务器内部错误响应
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return error(500, message);
    }
}
