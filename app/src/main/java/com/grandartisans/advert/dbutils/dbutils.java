package com.grandartisans.advert.dbutils;

import android.content.Context;
import android.util.Log;

import com.ljy.devring.util.FileUtil;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.List;

public class dbutils {
    final private static  String TAG = "dbutils";
    private static DbManager.DaoConfig daoConfig;
    private static DbManager dbManager = null;

    public static boolean init(Context context ){
        String dbPath = FileUtil.getExternalCacheDir(context);
        if(daoConfig == null){
            daoConfig = new DbManager.DaoConfig()
                    .setDbName("advert.db")   //设置数据库的名字
                    .setDbDir(new File(dbPath))   //设置数据库存储的位置
                    .setDbVersion(1)            //设置数据的版本号
                    .setAllowTransaction(true)  //设置是否允许开启事务
                    .setDbOpenListener(new DbManager.DbOpenListener() {
                        @Override
                        public void onDbOpened(DbManager db) {
                            // 开启WAL, 对写入加速提升巨大
                            db.getDatabase().enableWriteAheadLogging();
                        }
                    })
                    .setDbUpgradeListener(new DbManager.DbUpgradeListener() {//设置一个数据库版本升级的监听
                        @Override
                        public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                        }
                    });
            dbManager = x.getDb(daoConfig);//获取数据库单例
        }
        return true;
    }

    /**
     * 存储播放记录到数据库
     * @param record 需要存储的对象
     * @return
     */
    public static boolean insertPlayrecord(PlayRecord record) {
        if(dbManager!=null) {
            try {
                return dbManager.saveBindingId(record);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }else{
            Log.e(TAG,"dbManager is not inited ,please init it first");
        }
        return false;
    }

    /**
     * 根据模板ID和广告位ID和广告ID查询所有符合条件的记录
     * @param entity
     * @param tpid
     * @param apid
     * @param adid
     * @param <T>
     * @return
     */
    public static <T> List<T> selectById(Class<T> entity, long tpid, long apid,long adid){
        if(dbManager!=null) {
            try {
                return dbManager.selector(entity).where("tpid", "==", tpid).and("apid", "==", apid).and("adid", "==", adid).findAll();
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static <T> List<T> getPlayRecordAll(Class<T> entity){
        if(dbManager!=null) {
            try {
                return dbManager.selector(entity).findAll();
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 删除记录
     */
    public static<T> boolean deletePlayRecord(Class<T> entity,long id){
        if(dbManager!=null) {
            try {
                dbManager.deleteById(entity,id);
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 修改播放记录
     **/
    public static void updatePlayCount(PlayRecord entity){
        if(dbManager!=null) {
            Log.i(TAG,"updatePlayCount tpid =  " + entity.getTpid() + "apid = " + entity.getApid() + "adid = " + entity.getAdid());
            List<PlayRecord> records  = selectById(PlayRecord.class,entity.getTpid(),entity.getApid(),entity.getAdid());
            if(records==null || records.size()==0){
                insertPlayrecord(entity);
            }else {
                try {
                    entity.setId(records.get(0).getId());
                    entity.setCount(records.get(0).getCount()+1);
                    dbManager.update(entity, "count","endtime");
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Log.e(TAG,"update PlayCount error ,dbManager is not inited ,please init it first");
        }
    }
}
