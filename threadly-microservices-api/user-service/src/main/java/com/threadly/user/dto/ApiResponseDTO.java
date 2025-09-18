package com.threadly.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API response wrapper for Threadly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // ẩn field null để giảm payload
public class ApiResponseDTO<T> {

    private int status;

    private boolean success;

    private String message;

    private T data;

    private String error;

    private long timestamp;

    private String path;


    public static <T> com.threadly.common.dto.ApiResponseDTO<T> ok(T data, String path, String message, int status) {
        return com.threadly.common.dto.ApiResponseDTO.<T>builder()
                .status(status)
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .path(path)
                .build();
    }
    public static <T> com.threadly.common.dto.ApiResponseDTO<T> ok(T data, String path) {
        return com.threadly.common.dto.ApiResponseDTO.<T>builder()
                .status(200)
                .success(true)
                .message("Success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .path(path)
                .build();
    }



    public static com.threadly.common.dto.ApiResponseDTO<Void> error(int status, String message,
                                                                     String error, String path) {
        return com.threadly.common.dto.ApiResponseDTO.<Void>builder()
                .status(status)
                .success(false)
                .message(message)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .path(path)
                .build();
    }
}
