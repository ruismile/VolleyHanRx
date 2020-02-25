package com.hanrx.mobilesafe.volleyhanrx;

public class NewsPager {

    private String reason;

    private Pager result;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Pager getResult() {
        return result;
    }

    public void setResult(Pager result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "NewsPager{" +
                "reason='" + reason + '\'' +
                ", result=" + result +
                '}';
    }
}
