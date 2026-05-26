//package com.sc.costumException;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import java.time.LocalDateTime;
//
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class ApiResponse<T> {
//    private boolean success;
//    private String message;
//    private T data;
//    private LocalDateTime timestamp;
//    private Integer statusCode;
//    private String error;
//
//    // Constructors
//    public ApiResponse() {
//        this.timestamp = LocalDateTime.now();
//    }
//
//    public ApiResponse(boolean success, String message, T data) {
//        this();
//        this.success = success;
//        this.message = message;
//        this.data = data;
//    }
//
//    public ApiResponse(boolean success, String message, T data, Integer statusCode) {
//        this(success, message, data);
//        this.statusCode = statusCode;
//    }
//
//    public ApiResponse(boolean success, String message, String error, Integer statusCode) {
//        this();
//        this.success = success;
//        this.message = message;
//        this.error = error;
//        this.statusCode = statusCode;
//    }
//
//    // Static factory methods for success responses
//    public static <T> ApiResponse<T> success(T data) {
//        return new ApiResponse<>(true, "Operation successful", data, 200);
//    }
//
//    public static <T> ApiResponse<T> success(String message, T data) {
//        return new ApiResponse<>(true, message, data, 200);
//    }
//
//    public static <T> ApiResponse<T> created(T data) {
//        return new ApiResponse<>(true, "Resource created successfully", data, 201);
//    }
//
//    public static <T> ApiResponse<T> updated(T data) {
//        return new ApiResponse<>(true, "Resource updated successfully", data, 200);
//    }
//
//    public static <T> ApiResponse<T> deleted() {
//        return new ApiResponse<>(true, "Resource deleted successfully", null, 200);
//    }
//
//    // Static factory methods for error responses
//    public static <T> ApiResponse<T> error(String message) {
//        return new ApiResponse<>(false, message, null, 400);
//    }
//
//    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
//        return new ApiResponse<>(false, message, null, statusCode);
//    }
//
//    public static <T> ApiResponse<T> error(String message, String error, Integer statusCode) {
//        return new ApiResponse<>(false, message, error, statusCode);
//    }
//
//    public static <T> ApiResponse<T> notFound(String message) {
//        return new ApiResponse<>(false, message, null, 404);
//    }
//
//    public static <T> ApiResponse<T> validationError(String message, T validationErrors) {
//        return new ApiResponse<>(false, message, validationErrors, 400);
//    }
//
//    // Getters and Setters
//    public boolean isSuccess() { return success; }
//    public void setSuccess(boolean success) { this.success = success; }
//
//    public String getMessage() { return message; }
//    public void setMessage(String message) { this.message = message; }
//
//    public T getData() { return data; }
//    public void setData(T data) { this.data = data; }
//
//    public LocalDateTime getTimestamp() { return timestamp; }
//    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
//
//    public Integer getStatusCode() { return statusCode; }
//    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
//
//    public String getError() { return error; }
//    public void setError(String error) { this.error = error; }
//}