package com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces;

import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;


public interface IDownListener extends IHttpListener {

    void setHttpService(IHttpService httpService);

    void setCancelCalle();

    void setPauseCallble();

}
