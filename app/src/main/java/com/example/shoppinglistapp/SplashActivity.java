package com.example.shoppinglistapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SplashActivity extends AppCompatActivity {
    private boolean result;
    private Dialog dialog;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private ConnectionClass connectionClass;
    private Connection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        myTask();
    }
    public void myTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://google.com");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() == 200) {

                        result =  true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    result =  false;
                }

                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void run() {

                        if (!result) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                            builder.setMessage("Bağlantınızı kontrol edip tekrar deneyiniz");
                            builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    myTask();
                                }
                            });
                            builder.setNegativeButton("Çıkış", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                        } else {
                            sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                            editor = sharedPreferences.edit();
                            int userNo = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));

                            //Toast.makeText(SplashActivity.this,"user"+userNo,Toast.LENGTH_SHORT).show();
                            Intent intentLogin = new Intent(SplashActivity.this,Login.class);
                            Intent intentMain = new Intent(SplashActivity.this,MainActivity.class);

                            if(userNo == 0){
                                startActivity(intentLogin);
                                finish();
                            }else{
                                try {
                                    String query = "select * from Users where userID='"+userNo+"' and userLogin IS NOT NULL";
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet = statement.executeQuery(query);

                                    if(resultSet != null){
                                        while(resultSet.next()){
                                            String userImage = resultSet.getString("userProfileImage");
                                            String userName = resultSet.getString("userName");
                                            String userSurName = resultSet.getString("userSurName");

                                            intentMain.putExtra("userImage",userImage);
                                            intentMain.putExtra("userName",userName);
                                            intentMain.putExtra("userSurName",userSurName);

                                            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                            String dateTimeNow = format.format(LocalDateTime.now());
                                            String sqlDate = resultSet.getString("userLogin").substring(0,19);

                                            LocalDateTime dateTime = LocalDateTime.parse(dateTimeNow, format);
                                            LocalDateTime dateTime2 = LocalDateTime.parse(sqlDate, format);

                                            long diffInMinutes = Duration.between(dateTime, dateTime2).toMinutes();
                                            long diffInHours = Duration.between(dateTime, dateTime2).toHours();
                                            long diffInDays = Duration.between(dateTime, dateTime2).toDays();

                                            System.out.println("dakika: "+diffInMinutes);
                                            System.out.println("saat: "+diffInHours);
                                            System.out.println("gün: "+diffInDays);

                                            if(diffInDays < 1){
                                                if(diffInHours < 1){
                                                    if(diffInMinutes < 1){
                                                        startActivity(intentLogin);
                                                        finish();
                                                    }
                                                    else{
                                                        startActivity(intentMain);
                                                        finish();
                                                    }
                                                }
                                                else{
                                                    startActivity(intentMain);
                                                    finish();
                                                }
                                            }
                                            else{
                                                startActivity(intentMain);
                                                finish();
                                            }

                                        }

                                    }else{
                                        startActivity(intentLogin);
                                        finish();
                                    }
                                } catch (SQLException e) {
                                    System.out.println("Hata"+e);
                                    e.printStackTrace();
                                }
                            }

                        }
                    }
                });
            }
        }).start();
    }
}
