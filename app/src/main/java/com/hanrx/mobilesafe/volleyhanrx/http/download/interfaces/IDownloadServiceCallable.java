package com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces;

import com.hanrx.mobilesafe.volleyhanrx.http.download.DownLoadItemInfo;

public interface IDownloadServiceCallable {

    void onDownloadStatusChanged(DownLoadItemInfo downloadItemInfo);

    void onTotalLengthReceived(DownLoadItemInfo downloadItemInfo);

    void onCurrentSizeChanged(DownLoadItemInfo downloadItemInfo, double downLenth, long speed);

    void onDownloadSuccess(DownLoadItemInfo downLoadItemInfo);

    void onDownloadPause(DownLoadItemInfo downloadItemInfo);

    void onDownloadError(DownLoadItemInfo downLoadItemInfo, int var2, String var3);
}
