package com.hanrx.mobilesafe.volleyhanrx.http.download.dao;

import android.database.Cursor;

import com.hanrx.mobilesafe.volleyhanrx.db.BaseDao;
import com.hanrx.mobilesafe.volleyhanrx.http.download.DownLoadItemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DownDao extends BaseDao<DownLoadItemInfo> {

    //保存应该下载的集合   不包括已经下载成功的
    private List<DownLoadItemInfo> mDownLoadItemInfosList = Collections.synchronizedList(new ArrayList<DownLoadItemInfo>());

    private DownloadInfoComparator mDownloadInfoComparator = new DownloadInfoComparator();

    @Override
    protected String createTable() {
        return "create table if not exists  t_downloadInfo(" + "id Integer primary key, " +
                "url TEXT not null," + "filePath TEXT not null, " + "displayName TEXT, " +
                "status Integer, " + "totalLen Long, " + "currentLen Long," + "startTime TEXT," +
                "finishTime TEXT," + "userId TEXT, " + "httpTaskType TEXT," + "priority  Integer," +
                "stopMode Integer," + "downloadMaxSizeKey TEXT," + "unique(filePath))";
    }

    @Override
    public List<DownLoadItemInfo> query(String sql) {
        return null;
    }

    /**
     * 生成下载id
     *
     * @return 返回下载id
     */
    private Integer generateRecordId()
    {
        int maxId = 0;
        String sql = "select max(id)  from " + getTableName() ;
        synchronized (DownDao.class)
        {
            Cursor cursor = this.mDatabase.rawQuery(sql,null);
            if(cursor.moveToNext())
            {
                String[] colmName=cursor.getColumnNames();

                int index=cursor.getColumnIndex("max(id)");
                if(index!=-1)
                {
                    Object value =cursor.getInt(index);
                    if (value != null)
                    {
                        maxId = Integer.parseInt(String.valueOf(value));
                    }
                }
            }

        }
        return maxId + 1;
    }

    /**
     * 根据下载地址和下载文件路径查找下载记录
     *
     * @param url
     *            下载地址
     * @param filePath
     *            下载文件路径
     * @return
     */
    public DownLoadItemInfo findRecord(String url, String filePath) {
        synchronized (DownDao.class)
        {
            for (DownLoadItemInfo record : mDownLoadItemInfosList)
            {
                if (record.getUrl().equals(url) && record.getFilePath().equals(filePath))
                {
                    return record;
                }
            }
            /**
             * 内存集合找不到
             * 就从数据库中查找
             */
            DownLoadItemInfo where = new DownLoadItemInfo();
            where.setUrl(url);
            where.setFilePath(filePath);
            List<DownLoadItemInfo> resultList = super.query(where);
            if (resultList.size() > 0)
            {
                return resultList.get(0);
            }
            return null;
        }

    }

    /**
     * 根据 下载文件路径查找下载记录
     *
     *            下载地址
     * @param filePath
     *            下载文件路径
     * @return
     */
    public List<DownLoadItemInfo> findRecord(String filePath)
    {
        synchronized (DownDao.class)
        {
            DownLoadItemInfo where = new DownLoadItemInfo();
            where.setFilePath(filePath);
            List<DownLoadItemInfo> resultList = super.query(where);
            return resultList;
        }

    }



    /**
     * 比较器
     */
    class DownloadInfoComparator implements Comparator<DownLoadItemInfo>
    {
        @Override
        public int compare(DownLoadItemInfo lhs, DownLoadItemInfo rhs)
        {
            return rhs.getId() - lhs.getId();
        }
    }
}
