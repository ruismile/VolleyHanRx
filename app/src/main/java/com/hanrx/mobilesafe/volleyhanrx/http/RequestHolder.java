package com.hanrx.mobilesafe.volleyhanrx.http;

import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

public class RequestHolder<T> {
    //执行下载类
    private IHttpService mHttpService;

    //获取数据回调结果的类
    private IHttpListener mHttpListener;

    //请求参数对应的实体
    private T requestInfo;

    private String url;

    public IHttpService getHttpService() {
        return mHttpService;
    }

    public void setHttpService(IHttpService httpService) {
        mHttpService = httpService;
    }

    public IHttpListener getHttpListener() {
        return mHttpListener;
    }

    public void setHttpListener(IHttpListener httpListener) {
        mHttpListener = httpListener;
    }

    public T getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(T requestInfo) {
        this.requestInfo = requestInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
