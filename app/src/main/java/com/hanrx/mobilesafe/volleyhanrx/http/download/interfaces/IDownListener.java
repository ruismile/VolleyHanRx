package com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces;

import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;

import org.apache.http.protocol.HttpService;

public interface IDownListener extends IHttpListener {

    void setHttpService(HttpService httpService);

    void setCancelCalle();

    void setPauseCallble();

}
