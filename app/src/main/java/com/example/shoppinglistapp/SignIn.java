package com.example.shoppinglistapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SignIn extends AppCompatActivity {
    private Button buttonSignIn;
    private EditText editTextTextSignInName, editTextTextSignInSurName, editTextTextSignInEmailAddress, editTextTextSignInPassword;

    private ConnectionClass connectionClass;
    private Connection connection;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        buttonSignIn = findViewById(R.id.buttonSignIn);
        editTextTextSignInName = findViewById(R.id.editTextTextSignInName);
        editTextTextSignInSurName = findViewById(R.id.editTextTextSignInSurName);
        editTextTextSignInEmailAddress = findViewById(R.id.editTextTextSignInEmailAddress);
        editTextTextSignInPassword = findViewById(R.id.editTextTextSignInPassword);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editTextTextSignInName.getText().toString())){
                    Snackbar.make(view,"Ad boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextSignInSurName.getText().toString())){
                    Snackbar.make(view,"Soyad boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextSignInEmailAddress.getText().toString())){
                    Snackbar.make(view,"E-posta boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextSignInPassword.getText().toString())){
                    Snackbar.make(view,"Şifre boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    try{
                        String query = "select * from Users where userMail='"+editTextTextSignInEmailAddress.getText()+"'";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if (!resultSet.next()) {
                            try {
                                String query2 = "insert into Users (userName,userSurName,userMail,userPassword) values ('"+editTextTextSignInName.getText()+"', '"+editTextTextSignInSurName.getText()+"', '"+editTextTextSignInEmailAddress.getText()+"', '"+Encryption.encrypt(editTextTextSignInPassword.getText().toString())+"')";
                                PreparedStatement preparedStatement = connection.prepareStatement(query2);
                                preparedStatement.executeUpdate();
                                preparedStatement.close();
                                Snackbar.make(view,"Kayıt olundu",Snackbar.LENGTH_SHORT).show();
                                thread.start();
                            }catch (Exception e){
                                Snackbar.make(view,"Kayıt olunurken bir hatayla karşılaşıldı",Snackbar.LENGTH_SHORT).show();
                                System.out.println("Exception"+e);
                            }
                        }else{
                            Snackbar.make(view,"Bu mail ile üyelik bulunmaktadır",Snackbar.LENGTH_SHORT).show();
                        }

                    }catch (Exception e){
                        System.out.println("Exception"+e);
                    }
                }

            }
        });

    }
    Thread thread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                SignIn.this.finish();
                Intent intent = new Intent(SignIn.this,Login.class);
                startActivity(intent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}