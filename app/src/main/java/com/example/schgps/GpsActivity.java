package com.example.schgps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class GpsActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "GpsActivity";
    private boolean getService = false;     //是否已開啟定位服務
    private Location location = null;
    public Button btn_stop = null;
    public TextView txt_imei = null;
    public Button btn_delete = null;
    private LocationManager mLocationManager;//取得系統定位服務
    private String bestProvider = LocationManager.NETWORK_PROVIDER;
    private SQLiteDB mSQLiteDB = null;
    private String imei = null;
    private String tel = null;
    private String deviceNo = null;
    private String serialNo = null;
    private String getLocationTime = null;
    private SimpleDateFormat dateformat = null;
    private CheckPermission checkPermission = null;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        btn_stop = findViewById(R.id.btn_stop);
        btn_delete = findViewById(R.id.btn_delete);
        txt_imei = findViewById(R.id.txt_locate);
        checkPermission = new CheckPermission(this,this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);//取得系統定位服務
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            imei =  bundle.getString("imei");
            tel =  bundle.getString("tel");
            deviceNo = tel + imei;
        }

        getTime();

        try {
            serialNo = tel + dateformat.parse(getTime()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        openDB();
        mSQLiteDB.insert(serialNo,deviceNo,tel,imei,0.0 ,0.0,getTime(),"No");

        if (checkPermission.requestAskPermission() && checkPermission.checkSelfPermission()) {//詢問是否開啟權限 && 判斷使用者權限開啟否
            if(!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                openObject(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            }else{
                startKeepAliveService();
                getService = true;//確認已經開啟 網路 or GPS 定位
                //由程式判斷用GPS_PROVIDER OR NETWORK_PROVIDER
                if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (checkPermission.checkSelfPermission())
                    if (getService) {
                        mLocationManager.requestLocationUpdates(bestProvider, 500, 1, this);
                        showLog(location,"Init");
                        //                //服務提供者、更新頻率60000毫秒=1分鐘、最短距離1m、地點改變時呼叫物件
                    }
            }
        }

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(GpsActivity.this, ForegroundLocation.class);
                intent2.setAction(ForegroundLocation.ACTION_STOP_FOREGROUND_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e(TAG,"stopKeepAliveService");
                    startForegroundService(intent2);
                }
                if(getService) {
                    mLocationManager.removeUpdates(GpsActivity.this);
                }
                closeDB();
                Intent intent = new Intent(GpsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void openObject(String action) {
        Intent openGPS = new Intent(action);
        if (action.equals(Settings.ACTION_LOCATION_SOURCE_SETTINGS) && !mLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            startActivityForResult(openGPS, 0);
        }
    }

    private void getLocation(Location location) { //將定位資訊顯示在畫面中
//        if (checkSelfPermission())
//            if (getService) {
//                mLocationManager.requestLocationUpdates(bestProvider, 10000, 1, this);
//                //服務提供者、更新頻率60000毫秒=1分鐘、最短距離1m、地點改變時呼叫物件
//            }
//            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(location != null){
            showLog(location,"getLocation");
            Toast.makeText(this,"Toast (getLocation) 取得經度 : " + location.getLongitude() + " 取得緯度 : " + location.getLatitude() + "時間 : " + getTime(),Toast.LENGTH_SHORT).show();

            try {
                serialNo = tel + dateformat.parse(getTime()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //寫入資料庫欄位資訊
            txt_imei.setText("經度 : " + location.getLongitude() + " 緯度 : " + location.getLatitude() +"\n更新時間 : " + getTime());
            mSQLiteDB.insert(serialNo,deviceNo,tel,imei,location.getLongitude(),location.getLatitude(),getTime(),"No");
        }else{
            Log.i("(getLocation) 經緯度", "(getLocation) 尚未開啟定位服務" + " 時間 : " + getTime());
        }
    }

    public void startKeepAliveService(){
        Intent intent = new Intent(this, ForegroundLocation.class);
        intent.setAction(ForegroundLocation.ACTION_START_FOREGROUND_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG,"startKeepAliveService");
            ignoreBatteryOptimization(this);
            startForegroundService(intent);
        }
    }

    public void ignoreBatteryOptimization(Activity activity) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
            // 判断如果没有加入电池优化的白名单,则弹出加入电池优化的白名单的设置对话框
            if (!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void openDB(){
        mSQLiteDB = new SQLiteDB(this);
    }
    private void closeDB(){
        mSQLiteDB.close();
    }
    private void showLog(Location location,String point){
        if(location != null){
            Log.i("(" + point + ") 經緯度","serialNo : " + serialNo + " deviceNo : " + deviceNo + " 取得經度 : " + location.getLongitude() + " 取得緯度 : " + location.getLatitude() + " 時間 : " + getTime());
        }else{
            Log.i("(" + point + ") 經緯度","serialNo : " + serialNo + " deviceNo : " + deviceNo + " 取得經度 : 0 取得緯度 : 0 時間 : " + getTime());
        }

    }

    private String getTime(){
        dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        getLocationTime = dateformat.format(System.currentTimeMillis());
        return  getLocationTime;
    }
    @Override
    public void onLocationChanged(Location location) {  //當地點改變時
        // TODO 自動產生的方法 Stub
        getLocation(location);
    }

    @Override
    public void onProviderDisabled(String arg0) {//當GPS或網路定位功能關閉時
        // TODO 自動產生的方法 Stub
        Toast.makeText(this, "請開啟gps或3G網路", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String arg0) { //當GPS或網路定位功能開啟
        // TODO 自動產生的方法 Stub
        Toast.makeText(this, "已開啟gps或網路 by " + arg0, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) { //定位狀態改變
        // TODO 自動產生的方法 Stub
        Toast.makeText(this, "定位狀態改變" + arg0, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //離開頁面時也更新
//        if (checkSelfPermission())
//            if (getService) {
//                mLocationManager.requestLocationUpdates(bestProvider, 1000, 1, this);
////                //服務提供者、更新頻率60000毫秒=1分鐘、最短距離1m、地點改變時呼叫物件
//            }
//        location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        if(location != null){
//        mSQLiteDB.insert(imei,"" + location.getLongitude() ,"" + location.getLatitude(),getTime());
//            cursor = mSQLiteDB.select();
//            cursorAdapter = new SimpleCursorAdapter(GpsActivity.this, android.R.layout.simple_list_item_2, cursor, new String[]{"imei", "location"}, new int[]{android.R.id.text1, android.R.id.text2}, 0);
//            listView.setAdapter(cursorAdapter);
//            Log.i("(onResume) 經緯度", "取得經度 : " + location.getLongitude() + " 取得緯度 : " + location.getLatitude() + " 時間 : " + getTime());
//        }else{
//            Log.i("(onResume) 經緯度", "(onResume) 尚未開啟定位服務" + " 時間 : " + getTime());
//        }

    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //在頁面時停止更新
//        if(getService) {
//            mLocationManager.removeUpdates(this);
//        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(getService) {
            mLocationManager.removeUpdates(this);
        }
        closeDB();
    }
}
