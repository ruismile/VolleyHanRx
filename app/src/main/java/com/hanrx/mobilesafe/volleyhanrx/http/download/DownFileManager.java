package com.hanrx.mobilesafe.volleyhanrx.http.download;

import android.os.Environment;
import android.util.Log;

import com.hanrx.mobilesafe.volleyhanrx.http.HttpTask;
import com.hanrx.mobilesafe.volleyhanrx.http.RequestHolder;
import com.hanrx.mobilesafe.volleyhanrx.http.ThreadPoolManager;
import com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces.IDownloadServiceCallable;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import java.io.File;
import java.util.Map;
import java.util.concurrent.FutureTask;

public class DownFileManager implements IDownloadServiceCallable {
    private static final String TAG ="hanrx";
    private byte[] lock = new byte[0];
    /**
     * 下载
     * @param url
     */
    public void down(String url) {
        synchronized (lock) {
            String[] preFixs = url.split("/");
            String afterFix = preFixs[preFixs.length - 1];
            File file = new File(Environment.getExternalStorageDirectory(), afterFix);
            //实例化DownloadItem
            DownLoadItemInfo downLoadItemInfo = new DownLoadItemInfo(url, file.getAbsolutePath());
            RequestHolder requestHolder = new RequestHolder();
            //设置请求下载的策略
            IHttpService httpService = new FileDownHttpService();
            //得到请求头的参数 map
            Map<String, String> map = httpService.getHttpHeadMap();

            //处理结果的策略
            IHttpListener httpListener = new DownLoadListener(downLoadItemInfo, this, httpService);
            requestHolder.setHttpListener(httpListener);
            requestHolder.setHttpService(httpService);
            HttpTask httpTask = new HttpTask(requestHolder);

            try {
                ThreadPoolManager.getInstance().execte(new FutureTask<Object>(httpTask, null));
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDownloadStatusChanged(DownLoadItemInfo downloadItemInfo) {

    }

    @Override
    public void onTotalLengthReceived(DownLoadItemInfo downloadItemInfo) {

    }

    @Override
    public void onCurrentSizeChanged(DownLoadItemInfo downloadItemInfo, double downLenth, long speed) {
        Log.i(TAG,"下载速度："+ speed/1000 +"k/s");
        Log.i(TAG,"-----路径  "+ downloadItemInfo.getFilePath()+"  下载长度  "+downLenth+"   速度  "+speed);
    }

    @Override
    public void onDownloadSuccess(DownLoadItemInfo downLoadItemInfo) {
        Log.i(TAG,"下载成功    路劲  "+ downLoadItemInfo.getFilePath()+"  url "+ downLoadItemInfo.getUrl());
    }

    @Override
    public void onDownloadPause(DownLoadItemInfo downloadItemInfo) {

    }

    @Override
    public void onDownloadError(DownLoadItemInfo downLoadItemInfo, int var2, String var3) {

    }
}
