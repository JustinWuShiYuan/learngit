package com.tools.payhelper.bean;

import java.io.Serializable;

public class ResultBean implements Serializable {
    private String message;
    private int status;

    public ResultBean() {
    }


    public ResultBean(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}
