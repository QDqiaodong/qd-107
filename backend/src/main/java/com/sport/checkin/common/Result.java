package com.sport.checkin.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;
    private List<String> warnings;

    private Result() {
        this.warnings = new ArrayList<>();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public Result<T> addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
        return this;
    }

    public Result<T> addWarnings(List<String> warnings) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        if (warnings != null && !warnings.isEmpty()) {
            this.warnings.addAll(warnings);
        }
        return this;
    }

}
