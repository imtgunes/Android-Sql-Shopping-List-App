package com.example.shoppinglistapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Login extends AppCompatActivity {
    private Button buttonLogin;
    private EditText editTextTextLoginEmailAddress, editTextTextLoginPassword;
    private TextView textViewRegister;

    private ConnectionClass connectionClass;
    private Connection connection;

    private int userID;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setStatusBarColor(getResources().getColor(R.color.loginBackground));

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();
        //Toast.makeText(Login.this,"conn"+connection,Toast.LENGTH_SHORT).show();
        buttonLogin = findViewById(R.id.buttonLogin);
        editTextTextLoginEmailAddress = findViewById(R.id.editTextTextLoginEmailAddress);
        editTextTextLoginPassword = findViewById(R.id.editTextTextLoginPassword);
        textViewRegister = findViewById(R.id.textViewRegister);

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(Login.this,SignIn.class);
                startActivity(signIn);
                //Login.this.finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(editTextTextLoginEmailAddress.getText().toString())){
                    Snackbar.make(view,"E-posta boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(editTextTextLoginPassword.getText().toString())){
                    Snackbar.make(view,"Şifre boş bırakılamaz",Snackbar.LENGTH_SHORT).show();
                }
                else{
                    try{
                        String query = "select * from Users where userMail='"+editTextTextLoginEmailAddress.getText()+"' and userPassword='"+Encryption.encrypt(editTextTextLoginPassword.getText().toString())+"' ";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if (resultSet.next() == false) {
                            Snackbar.make(view,"E-posta veya şifreniz hatalı",Snackbar.LENGTH_SHORT).show();

                        }else{
                            userID = resultSet.getInt("userID");
                            sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                            editor = sharedPreferences.edit();
                            editor.putString("userNo",Encryption.encrypt(String.valueOf(userID)));
                            editor.commit();
                            String userImage = resultSet.getString("userProfileImage");
                            String userName = resultSet.getString("userName");
                            String userSurName = resultSet.getString("userSurName");
                            Intent intent = new Intent(Login.this,MainActivity.class);
                            intent.putExtra("userImage",userImage);
                            intent.putExtra("userName",userName);
                            intent.putExtra("userSurName",userSurName);
                            //Toast.makeText(Login.this,"id"+Encryption.encrypt(String.valueOf(userID)),Toast.LENGTH_SHORT).show();
                            try {
                                LocalDateTime localDateTime = LocalDateTime.now();
                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
                                String dateTimeNow = dateTimeFormatter.format(localDateTime.plusYears(1));
                                System.out.println(dateTimeNow);
                                String query2 = "UPDATE Users SET userLogin = '"+dateTimeNow+"' WHERE userID='"+userID+"'";
                                PreparedStatement preparedStatement = connection.prepareStatement(query2);
                                preparedStatement.executeUpdate();
                                preparedStatement.close();
                                startActivity(intent);
                                Login.this.finish();

                            }catch (Exception e){
                                System.out.println("Exception"+e);
                            }
                        }


                    }catch (Exception e){
                        System.out.println("Exception"+e);
                    }
                }
            }
        });

    }
}