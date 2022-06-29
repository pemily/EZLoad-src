package com.pascal.ezload.service.rules.update;

public class PushResult {

    private String msg;
    private boolean success;

    public PushResult(){
    }

    public PushResult(boolean success, String msg){
        this.msg = msg;
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
