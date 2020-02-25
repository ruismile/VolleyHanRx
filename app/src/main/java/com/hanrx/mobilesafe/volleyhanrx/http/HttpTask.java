package com.hanrx.mobilesafe.volleyhanrx.http;

import com.alibaba.fastjson.JSON;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

public class HttpTask<T> implements Runnable{

    private IHttpService mHttpService;

    public HttpTask(RequestHolder<T> requestHolder) {
        mHttpService = requestHolder.getHttpService();
        mHttpService.setHttpListener(requestHolder.getHttpListener());
        mHttpService.setUrl(requestHolder.getUrl());
        //增加方法
        IHttpListener httpListener = requestHolder.getHttpListener();
        httpListener.addHttpHeader(mHttpService.getHttpHeadMap());
        try {
            T requeset = requestHolder.getRequestInfo();
            if (requeset == null) {
                String requestInfo = JSON.toJSONString(requeset);
                mHttpService.setRequestData(requestInfo.getBytes("UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        mHttpService.excute();
    }
}
