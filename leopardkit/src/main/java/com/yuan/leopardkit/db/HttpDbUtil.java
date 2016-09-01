package com.yuan.leopardkit.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yuan.leopardkit.db.dao.DaoMaster;
import com.yuan.leopardkit.db.dao.DaoSession;
import com.yuan.leopardkit.db.dao.FileModelDao;
import com.yuan.leopardkit.models.FileLoadInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;



/**
 * Created by Yuan on 2016/8/31.
 * Detail 文件上传与文件下载 数据库操作工具类
 */
public class HttpDbUtil {

    private final String TAG = "HttpDbUtil";

    public static HttpDbUtil instance;

    private Context context;
    private FileModelDao modelDao;
    private SQLiteDatabase sqLiteDatabase;

    private List<FileLoadInfo> refresList = new ArrayList<>();//更新队列
    private final long UPDATE_TIME = 1000 * 3;//3S

    public HttpDbUtil(Context context) {
        this.context = context;
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, "file-db", null);
        sqLiteDatabase = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(sqLiteDatabase);
        DaoSession daoSession = daoMaster.newSession();
        modelDao = daoSession.getFileModelDao();
    }

    public static HttpDbUtil initHttpDB(Context context) {
        if (instance == null) {
            instance = new HttpDbUtil(context);
        }
        return instance;
    }

    private HttpDbUtil checkNULL() {
        if (context == null) {
            Log.e(TAG, "context null");
            return null;
        }
        if (instance == null) {
            instance = new HttpDbUtil(this.context);
        }
        return instance;
    }

    public List<FileModel> queryFileInfo() {
        List<FileModel> data = checkNULL().modelDao.queryRaw("where 1", "",null);
        return data;
    }

    /**
     * 插入一条下载or上传记录
     *
     * @param info
     * @return key 行号
     */
    public long insert(FileLoadInfo info) {
        long index = checkNULL().modelDao.insert(new FileModel(info.getType(), info.getState()
                , info.getUrl(), info.getFileName(), info.getFileSavePath(), info.getProgress(), info.getFileLength()));
        return index;
    }

    /**
     * 更新下载Or上传状态 有延时
     * @param info
     */
    public void updateState(FileLoadInfo info) {
        WeakHashMap<Object, Long> map = null;
        if (info.getRefreshTime() == 0L) {
            info.setRefreshTime(System.currentTimeMillis());
            refresList.add(info);
            update(info);
        } else {
            //检测队列更新时间
            if (refresList.contains(info)) {
                if (System.currentTimeMillis() - info.getRefreshTime() > UPDATE_TIME) {
                    update(info);
                }
            } else {
                info.setRefreshTime(System.currentTimeMillis());
                refresList.add(info);
                update(info);
            }
        }
    }

    /**
     * 更新通用方法，无延时
     * @param info
     */
    public void update(FileLoadInfo info) {
        checkNULL().modelDao.update(new FileModel(info.getKey(), info.getType(), info.getState()
                , info.getUrl(), info.getFileName(), info.getFileSavePath(), info.getProgress(), info.getFileLength()));
    }

    /**
     * 删除下载or上传记录
     * @param info
     */
    public void delete(FileLoadInfo info) {
        checkNULL().modelDao.delete(new FileModel(info.getKey(), info.getType(), info.getState()
                , info.getUrl(), info.getFileName(), info.getFileSavePath(), info.getProgress(), info.getFileLength()));
    }

    public void delete(FileModel model){
        checkNULL().modelDao.delete(model);
    }
}
