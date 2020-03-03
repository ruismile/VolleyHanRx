package com.hanrx.mobilesafe.volleyhanrx.http.download;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hanrx.mobilesafe.volleyhanrx.db.BaseDaoFactory;
import com.hanrx.mobilesafe.volleyhanrx.http.HttpTask;
import com.hanrx.mobilesafe.volleyhanrx.http.RequestHolder;
import com.hanrx.mobilesafe.volleyhanrx.http.download.dao.DownDao;
import com.hanrx.mobilesafe.volleyhanrx.http.download.enmus.DownloadStatus;
import com.hanrx.mobilesafe.volleyhanrx.http.download.enmus.DownloadStopMode;
import com.hanrx.mobilesafe.volleyhanrx.http.download.enmus.Priority;
import com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces.IDownloadCallable;
import com.hanrx.mobilesafe.volleyhanrx.http.download.interfaces.IDownloadServiceCallable;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpListener;
import com.hanrx.mobilesafe.volleyhanrx.http.interfaces.IHttpService;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public class DownFileManager implements IDownloadServiceCallable {
    private static final String TAG ="hanrx";
    private byte[] lock = new byte[0];

    DownDao mDownDao = BaseDaoFactory.getInstance().getDataHelper(DownDao.class, DownLoadItemInfo.class);

    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    /**
     * 观察者模式
     */
    private final List<IDownloadCallable> applisteners = new CopyOnWriteArrayList<IDownloadCallable>();

    /**
     * 怎在下载的所有任务
     */
    private static List<DownLoadItemInfo> downloadFileTaskList = new CopyOnWriteArrayList();

    Handler mHandler=new Handler(Looper.getMainLooper());

    public int download(String url) {
        String[] preFix=url.split("/");
        return this.download(url,Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+preFix[preFix.length-1]);
    }

    public int download(String url, String filePath ) {
        String[] preFix=url.split("/");
        String displayName=preFix[preFix.length-1];
        return this.download(url,filePath,displayName);
    }

    public int download(String url, String filePath, String displayName) {
        return this.download(url,filePath,displayName,Priority.middle);
    }

    public int download(String url, String filePath,
                        String displayName , Priority priority ) {
        if (priority == null) {
            priority = Priority.low;
        }
        File file = new File(filePath);
        DownLoadItemInfo downLoadItemInfo = null;
        downLoadItemInfo = mDownDao.findRecord(url, filePath);
        //没下载
        if (downLoadItemInfo == null) {
            //根据文件路径查找
            List<DownLoadItemInfo> samesFile = mDownDao.findRecord(filePath);

            //大于0表示下载
            if (samesFile.size() > 0) {
                DownLoadItemInfo sameDown = samesFile.get(0);
                if (sameDown.getCurrentLen() == sameDown.getTotalLen()) {
                    synchronized (applisteners) {
                        for (IDownloadCallable downloadCallable : applisteners) {
                            downloadCallable.onDownloadError(sameDown.getId(), 2, "文件已经下载了");
                        }
                    }
                }
                //插入数据库,可能插入失败,因为filePath  和id是独一无二的  在数据库建表时已经确定了
                int recrodId=mDownDao.addRecrod(url,filePath,displayName,priority.getValue());
                if(recrodId!=-1)
                {
                    synchronized (applisteners)
                    {
                        for (IDownloadCallable downloadCallable:applisteners)
                        {
                            //通知应用层  数据库被添加了
                            downloadCallable.onDownloadInfoAdd(downLoadItemInfo.getId());
                        }
                    }
                }
                //插入失败时，再次进行查找，确保能查得到
                else
                {
                    //插入
                    downLoadItemInfo=mDownDao.findRecord(url,filePath);
                }
            }
            /**-----------------------------------------------
             * 括号写错了  放在外面
             *
             * 是否正在下载`
             */
            if(isDowning(file.getAbsolutePath()))
            {
                synchronized (applisteners)
                {
                    for (IDownloadCallable downloadCallable:applisteners)
                    {
                        downloadCallable.onDownloadError(downLoadItemInfo.getId(),4,"正在下载，请不要重复添加");
                    }
                }
                return downLoadItemInfo.getId();
            }

            if(downLoadItemInfo!=null)
            {
                downLoadItemInfo.setPriority(priority.getValue());
                //添加----------------------------------------------------
                downLoadItemInfo.setStopMode(DownloadStopMode.auto.getValue());

                //判断数据库存的 状态是否是完成
                if(downLoadItemInfo.getStatus()!= DownloadStatus.finish.getValue())
                {
                    if(downLoadItemInfo.getTotalLen()==0L||file.length()==0L)
                    {
                        Log.i(TAG,"还未开始下载");
                        //----------------------删除--------------------
                        downLoadItemInfo.setStatus(DownloadStatus.failed.getValue());
                    }
                    //判断数据库中 总长度是否等于文件长度
                    if(downLoadItemInfo.getTotalLen()==file.length()&&downLoadItemInfo.getTotalLen()!=0)
                    {
                        downLoadItemInfo.setStatus(DownloadStatus.finish.getValue());
                        synchronized (applisteners)
                        {
                            for (IDownloadCallable downloadCallable:applisteners)
                            {
                                try {
                                    downloadCallable.onDownloadError(downLoadItemInfo.getId(),4,"已经下载了");
                                }catch (Exception e)
                                {
                                }
                            }
                        }
                    }
                }
                //------------------添加--------
                else
                {
                    if(!file.exists()||(downLoadItemInfo.getTotalLen()!=downLoadItemInfo.getCurrentLen()))
                    {
                        downLoadItemInfo.setStatus(DownloadStatus.failed.getValue());
                    }
                }
                /**
                 *
                 * 更新
                 */
                mDownDao.updateRecord(downLoadItemInfo);
                //移到括号里面来----------------------------------------------------
                /**
                 * 判断是否已经下载完成
                 */
                if(downLoadItemInfo.getStatus()==DownloadStatus.finish.getValue())
                {
                    Log.i(TAG,"已经下载完成  回调应用层");
                    final int downId=downLoadItemInfo.getId();
                    synchronized (applisteners)
                    {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                for (IDownloadCallable downloadCallable:applisteners)
                                {
                                    downloadCallable.onDownloadStatusChanged(downId,DownloadStatus.finish);
                                }
                            }
                        });
                    }
                    mDownDao.removeRecordFromMemery(downId);
                    return downLoadItemInfo.getId();
                }//之前的下载 状态为暂停状态
                List<DownLoadItemInfo> allDowning=downloadFileTaskList;
                //当前下载不是最高级  则先退出下载
                if(priority!=Priority.high)
                {
                    for(DownLoadItemInfo downling:allDowning)
                    {
                        //从下载表中  获取到全部正在下载的任务
                        downling=mDownDao.findSigleRecord(downling.getFilePath());

                        if(downling!=null&&downling.getPriority()==Priority.high.getValue())
                        {

                            /**
                             *     更改---------
                             *     当前下载级别不是最高级 传进来的是middle    但是在数据库中查到路径一模一样 的记录   所以他也是最高级------------------------------
                             *     比如 第一次下载是用最高级下载，app闪退后，没有下载完成，第二次传的是默认级别，这样就应该是最高级别下载

                             */
                            if (downling.getFilePath().equals(downLoadItemInfo.getFilePath()))
                            {
                                break;
                            }
                            else
                            {
                                return downLoadItemInfo.getId();
                            }
//                        if(downloadItemInfo.getFilePath().equals(downling.getFilePath()))
//                        {
//                            return downloadItemInfo.getId();
//                        }
                        }
                    }
                }
                //
                reallyDown(downLoadItemInfo);
                if(priority==Priority.high||priority== Priority.middle)
                {
                    synchronized (allDowning)
                    {
                        for (DownLoadItemInfo downloadItemInfo1:allDowning)
                        {
                            if(!downLoadItemInfo.getFilePath().equals(downloadItemInfo1.getFilePath()))
                            {
                                DownLoadItemInfo downingInfo=mDownDao.findSigleRecord(downloadItemInfo1.getFilePath());
                                if(downingInfo!=null)
                                {
                                    pause(downLoadItemInfo.getId(),DownloadStopMode.auto);
                                }
                            }
                        }
                    }
                    return downLoadItemInfo.getId();
                }
            }
        }
        return  -1;
    }

    /**
     * 停止
     * @param downloadId
     * @param mode
     */
    public void pause(int downloadId, DownloadStopMode mode)
    {
        if (mode == null)
        {
            mode = DownloadStopMode.auto;
        }
        final DownLoadItemInfo downloadInfo =mDownDao.findRecordById(downloadId);
        if (downloadInfo != null)
        {
            // 更新停止状态
            if (downloadInfo != null)
            {
                downloadInfo.setStopMode(mode.getValue());
                downloadInfo.setStatus(DownloadStatus.pause.getValue());
                mDownDao.updateRecord(downloadInfo);
            }
            for (DownLoadItemInfo downing:downloadFileTaskList)
            {
                if(downloadId==downing.getId())
                {
                    downing.getHttpTask().pause();
                }
            }
        }
    }

    //判断当前任务是否在下载
    private boolean isDowning(String absolutePath) {

        for (DownLoadItemInfo downloadItemInfo: downloadFileTaskList)
        {
            if(downloadItemInfo.getFilePath().equals(absolutePath))
            {
                return true;
            }
        }
        return false;
    }

    public void setDownCallable(IDownloadCallable downloadCallable) {
        synchronized (applisteners) {
            //添加观察者
            applisteners.add(downloadCallable);
        }
    }

    /**
     * 下载
     */
    public DownLoadItemInfo reallyDown(DownLoadItemInfo downloadItemInfo)
    {
        synchronized (lock)
        {
            //实例化DownloadItem
            RequestHolder requestHodler=new RequestHolder();
            //设置请求下载的策略
            IHttpService httpService=new FileDownHttpService();
            //得到请求头的参数 map
            Map<String,String> map=httpService.getHttpHeadMap();
            /**
             * 处理结果的策略
             */
            IHttpListener httpListener=new DownLoadListener(downloadItemInfo,this,httpService);

            requestHodler.setHttpListener(httpListener);
            requestHodler.setHttpService(httpService);
            /**
             *  bug  url
             */
            requestHodler.setUrl(downloadItemInfo.getUrl());

            HttpTask httpTask=new HttpTask(requestHodler);
            downloadItemInfo.setHttpTask(httpTask);

            /**
             * 添加
             */
            downloadFileTaskList.add(downloadItemInfo);
            httpTask.run();
        }

        return downloadItemInfo;

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
