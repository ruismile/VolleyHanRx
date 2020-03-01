package com.hanrx.mobilesafe.volleyhanrx.http;

import com.alibaba.fastjson.JSON;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

public class HttpTask<T> implements Runnable{

    private IHttpService mHttpService;
    private FutureTask futureTask;

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

    /**
     * 新增方法
     */
    public void start()
    {
        futureTask=new FutureTask(this,null);
        try {
            ThreadPoolManager.getInstance().execte(futureTask);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 新增方法
     */
    public  void pause()
    {
        mHttpService.pause();
        if(futureTask!=null)
        {
            ThreadPoolManager.getInstance().removeTask(futureTask);
        }

    }
}
