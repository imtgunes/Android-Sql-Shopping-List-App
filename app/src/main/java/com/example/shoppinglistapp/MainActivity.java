package com.example.shoppinglistapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private boolean check = false;

    private ConnectionClass connectionClass;
    private Connection connection;

    private RecyclerView recyclerView;
    private ArrayList<Category> arrayListCategory;
    private CustomAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ProgressDialog progressDialog;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int userID;

    private TextView textViewUserName, textViewUserSurName ;
    private ImageView imageViewUserImage ;
    private ImageButton imageButtonUserLogOut;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
        userID = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        drawerLayout = findViewById(R.id.drawer);
        recyclerView = findViewById(R.id.recyclerViewMain);
        recyclerView.setHasFixedSize(true);

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.nav_open,R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setItemIconTintList(null);

        toolbar.setNavigationIcon(R.drawable.ic_baseline_sort_24);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.itemCart){
                    try{
                        String query = "select * from ShoppingCart where shoppingCartState='0' and shoppingCartUserID='"+userID+"'";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if (!resultSet.next()) {
                            View view = findViewById(android.R.id.content);
                            Snackbar.make(view,"Sepet boş",Snackbar.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(MainActivity.this,CartActivity.class);
                            startActivity(intent);
                        }

                    }catch (Exception e){
                        System.out.println("Exception"+e);
                    }
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }

                }
                else if(item.getItemId() == R.id.itemTodoList){
                    Intent intent = new Intent(MainActivity.this,toDoListActivity.class);
                    startActivity(intent);
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                else if(item.getItemId() == R.id.itemTodoListComplated){
                    Intent intent = new Intent(MainActivity.this,ToDoListActivityComplated.class);
                    startActivity(intent);
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)){
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        String userImage = getIntent().getStringExtra("userImage");
        String userName = getIntent().getStringExtra("userName");
        String userSurName = getIntent().getStringExtra("userSurName");

        textViewUserName = headerView.findViewById(R.id.textViewUserName);
        textViewUserSurName = headerView.findViewById(R.id.textViewUserSurName);
        textViewUserName.setText(userName);
        textViewUserSurName.setText(userSurName);
        imageViewUserImage = headerView.findViewById(R.id.imageViewUserImage);
        imageButtonUserLogOut = headerView.findViewById(R.id.imageButtonUserLogOut);

        Glide.with(MainActivity.this)
                .asBitmap()
                .load(userImage)
                .centerCrop()
                .into(imageViewUserImage);


        imageButtonUserLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("userNo","GuZMgQ2zRFt6sFV53NLtnA==");
                    editor.commit();
                    String query = "UPDATE Users SET userLogin = NULL WHERE userID='"+userID+"'";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    Intent intent = new Intent(MainActivity.this,SplashActivity.class);
                    startActivity(intent);
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        arrayListCategory = new ArrayList<>();
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
                        String query = "select * from Category";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if(resultSet != null){
                            while (resultSet.next()){
                                arrayListCategory.add(new Category(resultSet.getInt("categoryID"),resultSet.getString("categoryName"),resultSet.getString("categoryImage")));
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
                        if(!check){
                            System.out.println("An error occurred while retrieving data");
                        }
                        else {
                            try {
                                adapter = new CustomAdapter(arrayListCategory);
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
        private ArrayList<Category> productItemLists;
        public class ViewHolder extends RecyclerView.ViewHolder{
            private final TextView categoryName;
            private final ImageView categoryImage;
            public ViewHolder(View view){
                super(view);
                categoryName = view.findViewById(R.id.textViewCategoryItem);
                categoryImage = view.findViewById(R.id.imageViewToDoDetail);
            }
        }
        public CustomAdapter(ArrayList<Category> itemLists){
            productItemLists = itemLists;
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_category,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final Category category = productItemLists.get(position);
            holder.categoryName.setText(category.getCategoryName());
            Picasso.get().load(category.getCategoryImage()).into(holder.categoryImage);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this,ProductActivity.class);
                    intent.putExtra("categoryID",category.getCategoryID());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return productItemLists.size();
        }
    }

}