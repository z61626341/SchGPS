package com.example.schgps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CheckPermission {
    public final List<String> permissionsList = new ArrayList<>();
    public Context context;
    public Activity activity;

    public CheckPermission(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public boolean checkSelfPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
            return true;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_PHONE_STATE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionsList.size() < 1) {
            return true;
        } else {
            return false;
        }
    }

ddddd
    public boolean requestAskPermission() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M)
            return true;
        if (checkSelfPermission()) {
            return true;
        }else{
            activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0x00);
            permissionsList.clear();
            if (checkSelfPermission()) {
                return true;
            }else{
                Toast.makeText(context, "CheckPermission 請開啟gps和訪問電話權限", Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }


}
