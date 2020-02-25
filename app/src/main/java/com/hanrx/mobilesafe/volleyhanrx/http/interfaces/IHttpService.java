package com.hanrx.mobilesafe.volleyhanrx.http.interfaces;


import java.util.Map;

/**
 * 获取网络
 */
public interface IHttpService {

    /**
     * 设置url
     * @param url
     */
    void setUrl(String url);

    /**
     * 执行获取网络
     */
    void excute();

    /**
     * 设置处理接口
     * @param httpListener
     */
    void setHttpListener(IHttpListener httpListener);

    /**
     * 设置请求参数
     */
    void setRequestData(byte[] requestData);

    /**
     * 暂停
     */
    void pause();

    /**
     * 获取请求头的map
     * @return
     */
    Map<String, String> getHttpHeadMap();

    /**
     * 判断是否取消
     * @return
     */
    boolean isCancel();

    /**
     * 判断是否暂停
     * @return
     */
    boolean isPause();
}
