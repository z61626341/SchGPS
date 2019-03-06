package com.example.schgps;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class GpsList extends AppCompatActivity{
    private static final String TAG = "GpsList";
    private ArrayList<String> list_value = null;
    public ListView listView = null;
    public TextView txt_imei = null;
    public Button btn_delete = null;
    public Button btn_stop = null;
    public ListAdapter adapter = null;
    private SQLiteDB mSQLiteDB = null;
    private String imei = null;
    private String tel = null;
    private String deviceNo = null;
    private String serialNo = null;
    private Cursor cursor = null;
    private String getLocationTime = null;
    public SimpleCursorAdapter cursorAdapter = null;
    private SimpleDateFormat dateformat = null;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_list);

        listView = findViewById(R.id.list_view);
        btn_delete = findViewById(R.id.btn_delete);
        btn_stop = findViewById(R.id.btn_stop);
        txt_imei = findViewById(R.id.txt_imei);
        list_value = new ArrayList<String>();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            imei =  bundle.getString("imei");
            tel =  bundle.getString("tel");
            deviceNo = tel + imei;
        }
        adapter = new ArrayAdapter<String>(this , android.R.layout.simple_list_item_1 ,list_value);
        listView.setAdapter(adapter);

        getTime();
        try {
            serialNo = tel + dateformat.parse(getTime()).getTime();
            txt_imei.setText("DeviceNo : " + deviceNo);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        openDB();
        selectDB2();

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GpsList.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSQLiteDB.deleteAll();
                selectDB2();
            }
        });
    }

    private void openDB(){
        mSQLiteDB = new SQLiteDB(this);
    }
    private void closeDB(){
        mSQLiteDB.close();
    }
    private void selectDB2(){
        cursor = mSQLiteDB.select2();
        if (cursor != null && cursor.getCount() >= 0) {
            cursorAdapter = new SimpleCursorAdapter(GpsList.this, R.layout.mylistview, cursor,
                    new String[]{"_id","serialNo","telNo","longitude","latitude","updatetime"},
                    new int[]{R.id.txt_view, R.id.txt_view1, R.id.txt_view2,R.id.txt_view3,R.id.txt_view4,R.id.txt_view5}, 0);
            listView.setAdapter(cursorAdapter);
        }
    }
    private String getTime(){
        dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        getLocationTime = dateformat.format(System.currentTimeMillis());
        return  getLocationTime;
    }

    @SuppressLint("MissingPermission")
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
        // TODO Auto-generated method stub
        super.onDestroy();
        closeDB();
    }
}
