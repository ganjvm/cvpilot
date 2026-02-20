package dev.gan.cvpilot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String status, T data, ErrorDetail error) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>("error", null, new ErrorDetail(code, message));
    }
}
