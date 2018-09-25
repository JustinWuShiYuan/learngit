package com.tools.payhelper.bean;

import java.io.Serializable;

public class AccountBean implements Serializable {
    private String amount;
    private String url;

    public AccountBean() {
    }

    public AccountBean(String amount, String url) {
        this.amount = amount;
        this.url = url;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "{" + "amount='" + amount + '\'' + ", url='" + url + '\'' + '}'+"\n";
    }
}
