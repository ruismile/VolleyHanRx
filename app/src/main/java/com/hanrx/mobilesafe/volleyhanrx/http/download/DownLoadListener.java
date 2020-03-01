package com.hanrx.mobilesafe.volleyhanrx.http.download;

import android.os.Handler;
import android.os.Looper;

import com.hanrx.mobilesafe.volleyhanrx.http.download.enmus.DownloadStatus;
import com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces.IDownListener;
import com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces.IDownloadServiceCallable;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import org.apache.http.HttpEntity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class DownLoadListener implements IDownListener {

    private DownLoadItemInfo mDownLoadItemInfo;

    private File file;

    protected String url;

    private long breakPoint;

    private IDownloadServiceCallable mDownloadServiceCallable;

    private IHttpService mHttpService;

    //得到主线程
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public void addHttpHeader(Map<String, String> headerMap) {

        long length = getFile().length();
        if (length > 0L) {
            headerMap.put("RANGE", "bytes=" + length + "-");
        }
    }

    public DownLoadListener(DownLoadItemInfo downLoadItemInfo,
                            IDownloadServiceCallable downloadServiceCallable, IHttpService iHttpService) {
        this.mDownLoadItemInfo = downLoadItemInfo;
        this.mDownloadServiceCallable = downloadServiceCallable;
        this.mHttpService = iHttpService;
        this.file = new File(downLoadItemInfo.getFilePath());
        //得到已经下载的长度
        this.breakPoint = file.length();
    }

    public DownLoadListener(DownLoadItemInfo downLoadItemInfo) {
        this.mDownLoadItemInfo = downLoadItemInfo;
    }

    @Override
    public void setHttpService(IHttpService httpService) {
        this.mHttpService = httpService;
    }

    /**
     * 设置取消接口
     */
    @Override
    public void setCancelCalle() {

    }

    @Override
    public void setPauseCallble() {

    }

    @Override
    public void onSuccess(HttpEntity httpEntity) {
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        //用于计算每秒多少k
        long speed = 0L;
        //花费时间
        long useTime = 0L;
        //下载的长度
        long getLen = 0L;
        //接受的长度
        long receiveLen = 0L;
        boolean bufferLen = false;
        //得到下载的长度
        long dataLength = httpEntity.getContentLength();
        //单位时间下载的字节数
        long calcSpeedLen = 0L;
        //总数
        long totalLength = this.breakPoint + dataLength;
        //更新数量
        this.receviceTotalLength(totalLength);
        //更新状态
        this.downloadStatusChange(DownloadStatus.downloading);
        byte[] buffer = new byte[1024];
        int count = 0;
        long currentTime = System.currentTimeMillis();
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;

        try {
            if (!makeDir(this.getFile().getParentFile())) {
                mDownloadServiceCallable.onDownloadError(mDownLoadItemInfo,1,"创建文件夹失败");
            } else {
                fos = new FileOutputStream(this.getFile(), true);
                bos = new BufferedOutputStream(fos);
                int length = 1;
                while ((length = inputStream.read(buffer)) != -1) {
                    if (this.getHttpService().isCancel()) {
                        mDownloadServiceCallable.onDownloadError(mDownLoadItemInfo, 1, "用户取消了");
                        return;
                    }

                    if (this.getHttpService().isPause()) {
                        mDownloadServiceCallable.onDownloadError(mDownLoadItemInfo, 2, "用户暂停了");
                        return;
                    }

                    bos.write(buffer, 0, length);
                    getLen += (long) length;
                    receiveLen += (long) length;
                    calcSpeedLen += (long) length;
                    ++count;
                    if (receiveLen * 10L / totalLength >= 1L || count >= 5000) {
                        currentTime = System.currentTimeMillis();
                        useTime = currentTime - startTime;
                        startTime = currentTime;
                        speed = 1000L * calcSpeedLen / useTime;
                        count = 0;
                        calcSpeedLen = 0L;
                        receiveLen = 0L;
                        //应该保存到数据库
                        this.downloadLengthChange(this.breakPoint + getLen, totalLength, speed);
                    }
                }
                bos.close();
                inputStream.close();
                if (dataLength != getLen) {
                    mDownloadServiceCallable.onDownloadError(mDownLoadItemInfo, 3, "下载长度不相等");
                } else {
                    this.downloadLengthChange(this.breakPoint + getLen, totalLength, speed);
                    this.mDownloadServiceCallable.onDownloadSuccess(mDownLoadItemInfo.copy());
                }
            }
        } catch (IOException ioException) {
            if (this.getHttpService() != null) {
//                this.getHttpService().abortRequest();
            }
            return;
        } catch (Exception e) {
            if (this.getHttpService() != null) {
//                this.getHttpService().abortRequest();
            }
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }

                if (httpEntity != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 创建文件夹的操作
     * @param parentFile
     * @return
     */
    private boolean makeDir(File parentFile) {

        return parentFile.exists() && !parentFile.isFile()
                ? parentFile.exists() && parentFile.isDirectory() : parentFile.mkdirs();
    }


    private void downloadLengthChange(final long downlength, final long totalLength, final long speed) {

        mDownLoadItemInfo.setCurrentLength(downlength);

        if (mDownloadServiceCallable != null) {
            DownLoadItemInfo copyDownItemInfo = mDownLoadItemInfo.copy();
            synchronized (this.mDownloadServiceCallable) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onCurrentSizeChanged(mDownLoadItemInfo, downlength, speed);
                    }
                });
            }

        }
    }

    /**
     * 更改下载时的状态
     * @param downloading
     */
    private void downloadStatusChange(DownloadStatus downloading) {
        mDownLoadItemInfo.setStatus(downloading.getValue());
        final DownLoadItemInfo copyDownLoadItemInfo = mDownLoadItemInfo.copy();
        if (mDownloadServiceCallable != null) {
            synchronized (this.mDownloadServiceCallable) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onDownloadStatusChanged(copyDownLoadItemInfo);
                    }
                });
            }
        }
    }

    /**
     * 回调 长度的变化
     * @param totalLength
     */
    private void receviceTotalLength(long totalLength) {
        mDownLoadItemInfo.setCurrentLength(totalLength);
        final DownLoadItemInfo copyDownLoadItemInfo = mDownLoadItemInfo.copy();
        if (mDownloadServiceCallable != null) {
            synchronized (this.mDownloadServiceCallable) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadServiceCallable.onTotalLengthReceived(copyDownLoadItemInfo);
                    }
                });
            }
        }
    }


    public IHttpService getHttpService() {
        return mHttpService;
    }

    public File getFile() {
        return file;
    }

    @Override
    public void onFail() {

    }
}
