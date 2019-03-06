package com.example.schgps;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LoadPositions extends AsyncTask<Void,Void,Void> {

    private List<String> address = new ArrayList<String>();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        for (int i=0; i<address.size(); i++) {
            Log.i("資料庫",address.get(i));
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // URL  jdbc:oracle:thin:@10.1.1.70:1521:topprod
        String url = "jdbc:oracle:thin:@http://schjim.duckdns.org:1521:jimdb";
        // username
        String username = "system";
        // password
        String password = "jim930527";
        try {
            Connection con = DriverManager.getConnection(url, username, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT deviceno FROM sales_locate WHERE rownum = 1;"
            );
            while (rs.next()) {
                address.add(rs.getString("deviceno"));
            }

            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return null;
    }
}
