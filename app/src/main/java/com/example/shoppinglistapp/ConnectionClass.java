package com.example.shoppinglistapp;

import android.os.StrictMode;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionClass {
    private String login  = "imtg1";
    private String password = "123456789";
    private String ip = "StajProjesiVeritabani.mssql.somee.com";
    private String port = "1433";
    private String databaseName = "StajProjesiVeritabani";
    private String url = "";
    private Connection connection = null;

    public Connection connection()
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try{
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            url = "jdbc:jtds:sqlserver://"+ip+":"+port+";"+"databasename="+databaseName+";user="+login+";password="+password+";";
            connection = DriverManager.getConnection(url);
        }catch (Exception e){

        }
        return connection;
    }
}