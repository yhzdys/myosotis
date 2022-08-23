package com.yhzdys.myosotis.web.entity;

public class WebResponse {

    private static final String success_message = "success";
    private static final WebResponse empty_success = new WebResponse().setSuccess(true).setMessage(success_message);

    private boolean success;
    private String message;
    private Object data;

    public static WebResponse success() {
        return empty_success;
    }

    public static WebResponse success(Object data) {
        return new WebResponse().setSuccess(true).setMessage(success_message).setData(data);
    }

    public static WebResponse fail(String message) {
        return new WebResponse().setSuccess(false).setMessage(message);
    }

    public boolean isSuccess() {
        return success;
    }

    public WebResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public WebResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public Object getData() {
        return data;
    }

    public WebResponse setData(Object data) {
        this.data = data;
        return this;
    }
}
