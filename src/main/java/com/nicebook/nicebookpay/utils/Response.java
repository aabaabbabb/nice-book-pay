package com.nicebook.nicebookpay.utils;

public record Response<T>(
        int code,
        String message,
        T data
) {

    public static <T> Response<T> success(T data) {
        return new Response<>(0, "成功", data);
    }

    public static <T> Response<T> fail(int code, String message) {
        return new Response<>(code, message, null);
    }
}
