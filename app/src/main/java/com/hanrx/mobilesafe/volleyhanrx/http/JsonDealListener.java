package com.hanrx.mobilesafe.volleyhanrx.http;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IDataListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;

import org.apache.http.HttpEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @param <M> 对应响应类
 */

public class JsonDealListener<M> implements IHttpListener{

    private Class<M> response;

    /**
     * 回调调用层的接口
     */
    private IDataListener<M> mDataListener;

    Handler mHandler = new Handler(Looper.getMainLooper());

    public JsonDealListener(Class<M> response, IDataListener<M> dataListener) {
        this.response = response;
        this.mDataListener = dataListener;
    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
            //得到网络返回数据
            String content = getContent(inputStream);
            final M m = JSON.parseObject(content,response);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDataListener.onSuccess(m);
                }
            });
        } catch (IOException e) {
            mDataListener.onFail();
        }

    }

    @Override
    public void onFail() {
        mDataListener.onFail();
    }

    private String getContent(InputStream inputStream) {
        String content = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                mDataListener.onFail();
                System.out.println("Error = " + e.toString());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.out.println("Error = " + e.toString());
                }
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            mDataListener.onFail();
        }
        return content;
    }
}
