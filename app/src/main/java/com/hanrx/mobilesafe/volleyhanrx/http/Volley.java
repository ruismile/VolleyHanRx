package com.hanrx.mobilesafe.volleyhanrx.http;

import com.hanrx.mobilesafe.volleyhanrx.R;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IDataListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import java.util.concurrent.FutureTask;

public class Volley {
    /**
     *  暴露给调用层
     * @param <T> 请求参数类型
     * @param <M> 响应参数类型
     */
    public static <T, M> void sendRequest(T requestInfo, String url,
                                          Class<M> response, IDataListener dataListener) {
        RequestHolder<T> requestHolder = new RequestHolder<>();
        requestHolder.setUrl(url);
        IHttpService httpService = new JsonHttpService();
        IHttpListener httpListener = new JsonDealListener<>(response, dataListener);
        requestHolder.setHttpService(httpService);
        requestHolder.setHttpListener(httpListener);
        HttpTask<T> httpTask = new HttpTask<>(requestHolder);
        try {
            ThreadPoolManager.getInstance().execte(new FutureTask<Object>(httpTask, null));
        } catch (InterruptedException e) {
            dataListener.onFail();
        }
    }
}
