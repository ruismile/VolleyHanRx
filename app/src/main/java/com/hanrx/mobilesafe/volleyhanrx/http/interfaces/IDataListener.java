package com.hanrx.mobilesafe.volleyhanrx.http.interfaces;

public interface IDataListener<M> {
    /**
     * 回调结果
     * @param m
     */
    void onSuccess(M m);

    void onFail();
}
