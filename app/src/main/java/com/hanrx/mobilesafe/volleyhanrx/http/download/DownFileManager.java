package com.hanrx.mobilesafe.volleyhanrx.http.download;

import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import java.util.Map;

public class DownFileManager {


    /**
     * 下载
     * @param url
     */
    public void down(String url) {

        IHttpService httpService = new FileDownHttpService();
        Map<String, String> map = httpService.getHttpHeadMap();
    }
}
