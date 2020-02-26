package com.hanrx.mobilesafe.volleyhanrx.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class BaseDaoFactory {
    private String mSqliteDatebasePath;

    private SQLiteDatabase mSQLiteDatabase;

    private static BaseDaoFactory instance = new BaseDaoFactory();

    public BaseDaoFactory() {
        mSqliteDatebasePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/logic.db";
        openDatebase();
    }

    public synchronized <T extends BaseDao<M>, M> T getDataHelper(Class<T> clazz, Class<M> entityClass) {
        BaseDao baseDao = null;
        try {
            baseDao = clazz.newInstance();
            baseDao.init(entityClass, mSQLiteDatabase);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return (T)baseDao;
    }

    private void openDatebase() {
        this.mSQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(mSqliteDatebasePath, null);
    }

    public static BaseDaoFactory getInstance() {
        return instance;
    }
}
