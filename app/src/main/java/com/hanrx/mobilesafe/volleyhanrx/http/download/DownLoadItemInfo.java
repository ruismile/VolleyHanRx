package com.hanrx.mobilesafe.volleyhanrx.http.download;

import com.hanrx.mobilesafe.volleyhanrx.http.HttpTask;

public class DownLoadItemInfo extends BaseEntity<DownLoadItemInfo> {
    private long currentLength;

    private long totalLength;

    private String url;

    private String filePath;

    private transient HttpTask mHttpTask;

    private DownloadStatus mStatus;

    //下载状态
    public DownloadStatus getStatus() {
        return mStatus;
    }

    public void setStatus(DownloadStatus status) {
        mStatus = status;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public HttpTask getHttpTask() {
        return mHttpTask;
    }

    public void setHttpTask(HttpTask httpTask) {
        mHttpTask = httpTask;
    }
}
