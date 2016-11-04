package com.websocket;

/**
 * Created by leiyang on 2016/10/27.
 */
public class UserMessage {
    private Boolean isClient;
    private String fromName;
    private String toName;
    private String msg;

    public Boolean getClient() {
        return isClient;
    }

    public void setClient(Boolean client) {
        isClient = client;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
