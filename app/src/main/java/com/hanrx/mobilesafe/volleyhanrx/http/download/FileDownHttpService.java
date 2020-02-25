package com.hanrx.mobilesafe.volleyhanrx.http.download;

import android.util.Log;

import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 请求下载文件的策略
 */
public class FileDownHttpService implements IHttpService {

    private static final String TAG = "hanrx";

    //即将添加到请求头的信息
    private Map<String, String> headerMap = Collections.synchronizedMap(new HashMap<String, String>());

    //含有请求处理的接口
    private IHttpListener mHttpListener;

    private HttpClient mHttpClient = new DefaultHttpClient();
    private HttpPost mHttpPost;
    private String url;

    private byte[] mRequestData;

    /**
     * httpClient 获取网络的回调
     */
    private HttpResponseHandler mHttpResponseHandler = new HttpResponseHandler();

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void excute() {
        constructHeader();
        mHttpPost = new HttpPost(url);
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(mRequestData);
        mHttpPost.setEntity(byteArrayEntity);
        try {
            mHttpClient.execute(mHttpPost, mHttpResponseHandler);
        } catch (IOException e) {
            mHttpListener.onFail();
        }
    }

    private void constructHeader() {
        Iterator iterator = headerMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = headerMap.get(key);
            Log.i(TAG, "请求头信息 " + key + " value " + value);
            mHttpPost.addHeader(key, value);
        }
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {

    }

    @Override
    public void setRequestData(byte[] requestData) {

    }

    @Override
    public void pause() {

    }

    @Override
    public Map<String, String> getHttpHeadMap() {
        return null;
    }

    @Override
    public boolean isCancel() {
        return false;
    }

    @Override
    public boolean isPause() {
        return false;
    }

    private class HttpResponseHandler extends BasicResponseHandler {
        @Override
        public String handleResponse(HttpResponse response) throws IOException {
            //响应码
            int code = response.getStatusLine().getStatusCode();
            if (code == 200) {
                mHttpListener.onSuccess(response.getEntity());
            } else {
                mHttpListener.onFail();
            }
            return null;
        }
    }
}
