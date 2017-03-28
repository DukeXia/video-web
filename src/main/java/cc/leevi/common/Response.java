package cc.leevi.common;

import java.io.Serializable;

/**
 * Created by jiang on 2017-03-24.
 */
public class Response<T> implements Serializable {
    private boolean success = true;
    private String msg = "success";
    private int code = 0;
    private T obj;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
