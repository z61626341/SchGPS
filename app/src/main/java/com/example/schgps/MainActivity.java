package com.example.schgps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    OkHttpClient client = new OkHttpClient();
    public static final String API = "http://schjim.duckdns.org/GPSAPI/values/Update";
    public Button btn_start, btn_list, btn_send;
    public TextView tv_imei;
    private String IMEI = null;
    private String imeiIntent = null;
    private String telIntent = null;
    private String deviceNo = null;
    private TelephonyManager mTelManager ;
    private final List<String> permissionsList = new ArrayList<>();
    private static final String TAG = "MainActivity";
    private SQLiteDB mSQLiteDB = null;
    private Cursor cursor = null;
    private Locations locations = null;
    private CheckPermission checkPermission = null;
    private Bundle bundle = null;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_start = findViewById(R.id.btn_start);
        btn_list = findViewById(R.id.btn_list);
        btn_send = findViewById(R.id.btn_send);
        tv_imei = findViewById(R.id.tv_imei);
        mTelManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        checkPermission = new CheckPermission(this,this);
        bundle = new Bundle();

        if (checkPermission.requestAskPermission() && checkPermission.checkSelfPermission()) {//詢問是否開啟權限 && 判斷使用者權限開啟否
            if(mTelManager.getLine1Number() == null || mTelManager.getLine1Number() == ""){
                telIntent = "99999990000000";
                IMEI = "55555555";
            }else{
                imeiIntent = mTelManager.getDeviceId();
                telIntent = mTelManager.getLine1Number().substring(1);//去除+號
                deviceNo = telIntent + imeiIntent;
                IMEI = "手機電話 : " + telIntent + "\n" + "IMEI碼為 : " + mTelManager.getDeviceId();
            }
//            Toast.makeText(this,telIntent,Toast.LENGTH_SHORT).show();
            tv_imei.setText(IMEI);
            bundle.putString("imei", imeiIntent);
            bundle.putString("tel", telIntent);
        }

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GpsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });
        btn_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GpsList.class);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                List<Locations> list_locations = new ArrayList<Locations>();
                openDB();
                cursor = mSQLiteDB.select();
                if (cursor != null && cursor.getCount() >= 0) {
                    cursor.moveToFirst();
                    for(int i = 0; i < cursor.getCount(); i++){
//                        Locations locations = new Locations("8869140656161551751970000","886914065616355090083243511","886914065616","Kelly",48.691412,62.941223,"2019/03/04");
                        locations = new Locations(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getDouble(5),cursor.getDouble(6),cursor.getString(7),cursor.getString(8));
                        list_locations.add(locations);
                        cursor.moveToNext();
                    }
                }
                //分批上傳
                int range = 15;
                if(list_locations.size() >= range){
                    List<Locations> list_locations2 = new ArrayList<Locations>();
                    int index_i = 0;
                    //段數區間: 20,40,60
                    for(int i = 1; i <= list_locations.size()/range; i++){
                        //index:0~19, 20~39, 40~59, ....
                        for(int j = 0;j < range;j++){
                            index_i = (i-1) * (range) + j;
                            list_locations2.add(list_locations.get(index_i));
                        }
                        //上傳0~19, 20~39, 40~59, ....
                        String str_json_part = gson.toJson(list_locations2);
                        okHttpConnection(str_json_part);
                        //上傳結束時，將List清空，使index size都是20 ex: list(20) = {0~19}, list(20) = {20~39}, list(20) = {40~59}
                        list_locations2.clear();
                    }
                    //將剩下沒上傳的上傳
                    int index_i_last = list_locations.size() % range; //ex: 161 / 20 ==> 1
                    int index_i_last2 = list_locations.size() / range * range;//ex: 161 / 20 * 20 ==> 1 * 20 ==> 20
                    for(int i = 0;i < index_i_last; i++){
                        list_locations2.add(list_locations.get(index_i_last2 + i));
                    }
                    String str_json_last = gson.toJson(list_locations2);
                    okHttpConnection(str_json_last);

                }else{
                    String str_json = gson.toJson(list_locations);
                    okHttpConnection(str_json);
                }
                mSQLiteDB.deleteAll();
                Toast.makeText(MainActivity.this,"Uploading your data...",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void okHttpConnection(String str_json){
        Log.i("OKHTTP get json : ", str_json);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8"), str_json);
        Request request = new Request.Builder()
                .url(API)
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Log.i("OKHTTP sucess json : ", json);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("OKHTTP", "告知使用者連線失敗");
            }
        });
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void openDB(){
        mSQLiteDB = new SQLiteDB(this);
    }
    private void closeDB(){
        mSQLiteDB.close();
    }
}
