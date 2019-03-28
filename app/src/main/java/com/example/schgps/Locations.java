package com.example.schgps;

public class Locations {
    private String serialNo;
    private String deviceNo;
    private String telNo;
    private String imeiNo;
    private double longitude;
    private double latitude;
    private String updateTime;
    private String isApprove;


    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(String deviceNo) {
        this.deviceNo = deviceNo;
    }

    public String getImeiNo() {
        return imeiNo;
    }

    public void setImeiNo(String imeiNo) {
        this.imeiNo = imeiNo;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getIsApprove() {
        return isApprove;
    }

    public void setIsApprove(String isApprove) {
        this.isApprove = isApprove;
    }

    public void Locations(){

    }
    public Locations(String serialNo,String deviceNo, String telNo, String imeiNo, double longitude, double latitude, String updateTime,String isApprove) {
        this.serialNo = serialNo;
        this.deviceNo = deviceNo;
        this.telNo = telNo;
        this.imeiNo = imeiNo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.updateTime = updateTime;
        this.isApprove = isApprove;
    }
}
