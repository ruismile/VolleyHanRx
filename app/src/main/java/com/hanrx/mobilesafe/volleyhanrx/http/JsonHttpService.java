package com.hanrx.mobilesafe.volleyhanrx.http;

import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class JsonHttpService implements IHttpService{

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
        mHttpPost = new HttpPost(url);
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(mRequestData);
        mHttpPost.setEntity(byteArrayEntity);
        try {
            mHttpClient.execute(mHttpPost, mHttpResponseHandler);
        } catch (IOException e) {
            mHttpListener.onFail();
        }
    }

    @Override
    public void setHttpListener(IHttpListener httpListener) {
        this.mHttpListener = httpListener;
    }

    @Override
    public void setRequestData(byte[] requestData) {
        this.mRequestData = requestData;
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
