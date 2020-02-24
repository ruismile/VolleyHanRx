package com.hanrx.mobilesafe.volleyhanrx.http;

import com.alibaba.fastjson.JSON;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

public class HttpTask<T> implements Runnable{

    private IHttpService mHttpService;

    public HttpTask(RequestHolder<T> requestHolder) {
        mHttpService = requestHolder.getHttpService();
        mHttpService.setHttpListener(requestHolder.getHttpListener());
        mHttpService.setUrl(requestHolder.getUrl());
        T requeset = requestHolder.getRequestInfo();
        String requestInfo = JSON.toJSONString(requeset);
        try {
            mHttpService.setRequestData(requestInfo.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        mHttpService.excute();
    }
}
