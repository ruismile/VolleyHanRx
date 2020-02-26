package com.hanrx.mobilesafe.volleyhanrx.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.hanrx.mobilesafe.volleyhanrx.db.annotion.DbFiled;
import com.hanrx.mobilesafe.volleyhanrx.db.annotion.DbTable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseDao<T> implements IBaseDao<T> {

    /**
     * 持有数据库操作类引用
     */
    protected SQLiteDatabase mDatabase;
    /**
     * 保证实例化一次
     */
    private boolean isInit = false;
    /**
     * 持有操作数据库表所对应的java类型
     */
    private Class<T> entityClass;

    private String tableName;

    public String getTableName() {
        return tableName;
    }

    /**
     * 维护表名和成员变量名的映射关系
     * key -- 表明
     * value -- Filed类型
     */
    private HashMap<String, Field> cacheMap;

    /**
     * @param entity
     * @param sqLiteDatabase
     * @return
     * 实例化一次
     */
    protected boolean init(Class<T> entity, SQLiteDatabase sqLiteDatabase) {
        if (!isInit) {
            entityClass = entity;
            mDatabase = sqLiteDatabase;
            if (entity.getAnnotation(DbTable.class) == null) {
                tableName = entity.getClass().getSimpleName();
            } else {
                tableName = entity.getAnnotation(DbTable.class).value();
            }
            if (!mDatabase.isOpen()) {
                return false;
            }
            if (!TextUtils.isEmpty(createTable())) {
                mDatabase.execSQL(createTable());
            }
            cacheMap = new HashMap<>();
            initCacheMap();
            isInit = true;
        }
        return  isInit;
    }

    /**
     * 维护映射关系
     */
    private void initCacheMap() {
        String sql = "select * from " + this.tableName + " limit 1 , 0";
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery(sql, null);
            //表的列名数组
            String[] columnName = cursor.getColumnNames();
            //拿到Filed数组
            Field[] columnFields = entityClass.getFields();

            for (Field field : columnFields) {
                field.setAccessible(true);
            }
            //开始找对应关系
            for (String columnNames : columnName) {
                //如果找到对应Field就赋值给他
                Field columnField = null;
                for (Field field : columnFields) {
                    String fieldName = null;
                    if (field.getAnnotation(DbFiled.class) != null) {
                        fieldName = field.getAnnotation(DbFiled.class).value();
                    } else {
                        fieldName = field.getName();
                    }
                    //如果表的列名等于了成员变量的注解名字
                    if (columnNames.equals(fieldName)) {
                        columnField = field;
                        break;
                    }
                }
                //找到了对应关系
                if (columnField != null) {
                    cacheMap.put(columnNames, columnField);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

    }

    @Override
    public Long insert(T entity) {
        Map<String, String> map = getValues(entity);
        ContentValues values = getContentValues(map);
        Long result = mDatabase.insert(tableName,null,values);
        return result;
    }

    /**
     * 将map转换成ContentValues
     * @return
     */
    private ContentValues getContentValues(Map<String, String> map) {
        ContentValues contentValues = new ContentValues();
        Set keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);
            if (value != null) {
                contentValues.put(key, value);
            }
        }
        return contentValues;
    }


    private Map<String, String> getValues(T entity) {
        HashMap<String, String> result = new HashMap<>();
        Iterator<Field> filedsIterator = cacheMap.values().iterator();
        //循环遍历映射map的Field
        while (filedsIterator.hasNext()) {
            Field colmunToField = filedsIterator.next();
            String cacheKey = null;
            String cacheValue = null;
            if (colmunToField.getAnnotation(DbFiled.class) != null)  {
                cacheKey = colmunToField.getAnnotation(DbFiled.class).value();
            } else {
                cacheKey = colmunToField.getName();
            }
            try {
                if (null == colmunToField.get(entity)) {
                    continue;
                }
                cacheValue = colmunToField.get(entity).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            result.put(cacheKey,cacheValue);
        }
        return result;
    }

    @Override
    public int update(T entity, T where) {
        int result = -1;
        Map values = getValues(entity);
        //将条件对象转换Map
        Map whereClause = getValues(where);

        Condition condition = new Condition(whereClause);
        ContentValues contentValues = getContentValues(values);
        result = mDatabase.update(tableName, contentValues, condition.getWhereClause(), condition.getWhereArgs());
        return result;
    }

    /**
     * 封装修改语句
     */
    class Condition {
        public String getWhereClause() {
            return whereClause;
        }

        public void setWhereClause(String whereClause) {
            this.whereClause = whereClause;
        }

        public String[] getWhereArgs() {
            return whereArgs;
        }

        public void setWhereArgs(String[] whereArgs) {
            this.whereArgs = whereArgs;
        }

        //查询条件 name = ? && password = ?
        private String whereClause;
        private String[] whereArgs;
        public Condition(Map<String, String> whereClause) {
            ArrayList list = new ArrayList();
            StringBuilder builder = new StringBuilder();
            builder.append(" 1=1 ");
            Set keys = whereClause.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = whereClause.get(key);
                if (value != null) {
                    //拼接条件查询语句
                    builder.append(" and " + key + " =?");
                    list.add(value);
                }
            }
            this.whereClause = builder.toString();
            this.whereArgs = (String[]) list.toArray(new String[list.size()]);
        }

    }

    @Override
    public int delete(T where) {
        Map map = getValues(where);
        Condition condition = new Condition(map);
        int result = mDatabase.delete(tableName, condition.getWhereClause(), condition.getWhereArgs());
        return result;
    }

    @Override
    public List<T> query(T where) {

        return query(where, null, null, null);
    }

    @Override
    public List<T> query(T where, String orderBy, Integer startIndex, Integer limit) {
        Map map = getValues(where);
        String limitString = null;
        if (startIndex != null && limit != null) {
            limitString = startIndex + " , " + limit;
        }

        Condition condition = new Condition(map);
        Cursor cursor = mDatabase.query(tableName, null, condition.getWhereClause(),
                condition.getWhereArgs(), null,null,orderBy,limitString);
        List<T> result = getResult(cursor,where);
        cursor.close();
        return result;
    }

    private List<T> getResult(Cursor cursor, T where) {
        ArrayList list = new ArrayList();

        Object item;
        while(cursor.moveToNext()) {
            try {
                item = where.getClass().newInstance();
                Iterator iterator = cacheMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    //得到列名
                    String colomnName = (String) entry.getKey();
                    //然后以列名拿到 列名在游标的位置
                    Integer columnIndex = cursor.getColumnIndex(colomnName);
                    Field field = (Field) entry.getValue();
                    Class type = field.getType();
                    if(columnIndex != -1) {
                        if (type == String.class) {
                            //反射方式赋值
                            field.set(item, cursor.getString(columnIndex));
                        } else if (type == Double.class) {
                            field.set(item, cursor.getDouble(columnIndex));
                        } else if (type == Integer.class) {
                            field.set(item, cursor.getInt(columnIndex));
                        } else if (type == Long.class) {
                            field.set(item, cursor.getLong(columnIndex));
                        } else if (type == byte[].class) {
                            field.set(item, cursor.getBlob(columnIndex));
                        }else {
                            //不支持的类型
                            continue;
                        }
                    }
                }
                list.add(item);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    /**
     * 创建表
     */
    protected abstract String createTable();
}


