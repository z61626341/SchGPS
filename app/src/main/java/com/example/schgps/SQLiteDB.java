package com.example.schgps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;

public class SQLiteDB extends SQLiteOpenHelper
{
    //db的名稱
    public final static String DATABASE_NAME = "SQLITDB.db";
    private final static String _TableName = "sqldb";
    //db的版本
    public final static int DATABASE_VERSION = 1;

    public SQLiteDB(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //建立db的Table與Table裡的欄位項目
        String TABLE = "CREATE TABLE IF NOT EXISTS " + _TableName + " ("
                + "_id"  + " INTEGER primary key autoincrement, "
                + "serialNo" + " varchar(128) unique NOT NULL, "
                + "deviceNo" + " varchar(128)  NOT NULL, "
                + "telNo" + " varchar(20) , "
                + "imeiNo" + " varchar(128) , "
                + "longitude" + " DECIMAL(16,8) , "
                + "latitude" + " DECIMAL(16,8) , "
                + "updatetime" + " date "+ ");";

        db.execSQL(TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        final String SQL = "DROP TABLE " + _TableName;
        db.execSQL(SQL);
    }

    //指標，db指向sqldb的Table
    public Cursor select()
    {
        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(_TableName, null, null, null, null, null, null);
        Cursor cursor = db.rawQuery("SELECT _id ,serialNo,deviceNo,telNo,imeiNo,longitude,latitude,updatetime FROM " + _TableName + " ORDER BY _id DESC ",null);
        return cursor;
    }
    public Cursor select2()
    {
        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor cursor = db.query(_TableName, null, null, null, null, null, null);
        Cursor cursor = db.rawQuery(
                "SELECT _id ," +
                "'SerialNo : '||telNo||' '||substr(serialNo,length(telNo)+1,length(serialNo)) serialNo," +
//                "'SerialNo : '||substr(serialNo,1,instr(serialNo,imeiNo)-1)||' '||substr(serialNo,instr(serialNo,imeiNo)) serialNo," +
                "'電話 : '||telNo telNo," +
                "'經度 : '||longitude longitude," +
                "'緯度 : '||latitude latitude," +
                "'更新時間 : '||updatetime updatetime " +
                "FROM " + _TableName + " ORDER BY _id DESC ",null);
        return cursor;
    }

    //新增db Table內容
    public long insert(String serialNo,String deviceNo, String telNo, String imeiNo, Double longitude, Double latitude, String updatetime)
    {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH24:mm:ss");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("serialNo", serialNo);
        cv.put("deviceNo", deviceNo);
        cv.put("telNo", telNo);
        cv.put("imeiNo", imeiNo);
        cv.put("longitude", longitude);
        cv.put("latitude", latitude);
        cv.put("updatetime", updatetime);
        long row = db.insert(_TableName, null, cv);
        return row;
    }

    //刪除Table單筆資料，帶入id
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = "_id" + " = " + Integer.toString(id) ;
        db.delete(_TableName, where, null);
    }

    //刪除Table全部資料
    public void deleteAll()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + " sqldb" );
    }
}
