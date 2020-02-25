package com.hanrx.mobilesafe.volleyhanrx;

import java.util.List;

public class Pager {

    private String state;

    private List<News> data;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<News> getData() {
        return data;
    }

    public void setData(List<News> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Pager{" +
                "state='" + state + '\'' +
                ", data=" + data +
                '}';
    }
}
