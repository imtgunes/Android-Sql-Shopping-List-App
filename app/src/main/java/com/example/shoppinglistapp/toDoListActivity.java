package com.example.shoppinglistapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class toDoListActivity extends AppCompatActivity {
    private ConnectionClass connectionClass;
    private Connection connection;

    private ArrayList<ToDoList> arrayList;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private boolean check = false;
    private ProgressDialog progressDialog;

    private int userID;

    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
        userID = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        recyclerView = findViewById(R.id.recyclerViewToDoList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        arrayList = new ArrayList<>();
        myTask();

        progressDialog = ProgressDialog.show(this,"","Loading",true);
    }

    public void myTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection == null){
                        check = false;
                    }else{
                        String query = "select * from ToDoList where todoUserID = '"+userID+"' and todoState = '0' order by todoSaveDate desc";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if(resultSet != null){
                            while (resultSet.next()){
                                arrayList.add(new ToDoList(resultSet.getInt("todoID"), resultSet.getString("todoSaveDate")));
                            }
                            check = true;
                        }else{
                            check = false;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    check = false;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if(check == false){
                            System.out.println("An error occurred while retrieving data");
                        }
                        else {
                            try {
                                adapter = new CustomAdapter(arrayList);
                                recyclerView.setAdapter(adapter);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder>{
        private ArrayList<ToDoList> todoItemLists;
        public class ViewHolder extends RecyclerView.ViewHolder{
            private final TextView todoNumber;
            private final TextView todoSaveDate;
            private final ImageButton imageButtonToDoDelete;
            private final ImageButton imageButtonToDoComplate;
            public ViewHolder(View view){
                super(view);
                todoNumber = view.findViewById(R.id.textViewToDoNumber);
                todoSaveDate = view.findViewById(R.id.textViewToDoDate);
                imageButtonToDoDelete = view.findViewById(R.id.imageButtonToDoDelete);
                imageButtonToDoComplate = view.findViewById(R.id.imageButtonToDoComplate);
            }
        }
        public CustomAdapter(ArrayList<ToDoList> itemLists){
            todoItemLists = itemLists;
        }
        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_todo,parent,false);
            return new CustomAdapter.ViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final ToDoList toDoList = todoItemLists.get(position);
            holder.todoNumber.setText("to-do "+String.valueOf(toDoList.getTodoNumber()));
            holder.todoSaveDate.setText(toDoList.getTodoSaveDate().substring(0,16));
            holder.imageButtonToDoComplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String query = "update ToDolist set todoState = '1' where todoID='"+toDoList.getTodoNumber()+"'";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        arrayList.clear();
                        myTask();
                    }catch (Exception e){
                        System.out.println("Exception: "+e);
                    }

                }
            });
            holder.imageButtonToDoDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(toDoListActivity.this);
                    builder.setMessage("Listeyi siliyorsunuz");
                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                String query = "BEGIN\n" +
                                        "  DECLARE @id INT\n" +
                                        "  select @id=todoShoppingCartID FROM ToDoList WHERE todoID = '"+toDoList.getTodoNumber()+"';\n" +
                                        "  delete from ToDoList where todoID = '"+toDoList.getTodoNumber()+"'\n" +
                                        "  delete FROM ShoppingCart WHERE shoppingCartID = @id;\n" +
                                        "END";
                                PreparedStatement preparedStatement = connection.prepareStatement(query);
                                preparedStatement.executeUpdate();
                                arrayList.clear();
                                myTask();
                                Snackbar.make(view,"Liste Silindi",Snackbar.LENGTH_SHORT).show();
                            }catch (Exception e){

                            }
                        }
                    });
                    builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(toDoListActivity.this,toDoListDetailActivity.class);
                    intent.putExtra("todoID",toDoList.getTodoNumber());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return todoItemLists.size();
        }
    }
}