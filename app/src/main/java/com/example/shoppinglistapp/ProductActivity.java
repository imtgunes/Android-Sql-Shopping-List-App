package com.example.shoppinglistapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Build;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ProductActivity extends AppCompatActivity {
    private ConnectionClass connectionClass;
    private Connection connection;

    private ArrayList<Product> arrayListProduct;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;


    private boolean check = false;
    private ProgressDialog progressDialog;

    private FrameLayout frameLayout;
    private Fragment fragmentAddToCart;
    private FragmentManager fragmentManager;

    private SearchView searchView;

    private BottomNavigationView bottomNavigationView;

    private ImageButton imageButtonBasket;

    private int categoryID;

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        getWindow().setStatusBarColor(getResources().getColor(R.color.topLayoutBackground));

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        categoryID = getIntent().getIntExtra("categoryID",0);

        //Toast.makeText(this,""+connection,Toast.LENGTH_LONG).show();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        frameLayout = findViewById(R.id.frameLayaut);
        fragmentManager = getSupportFragmentManager();
        fragmentAddToCart = new FragmentAddToCart();

        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Search(newText);
                return false;
            }
        });

        imageButtonBasket = findViewById(R.id.imageButtonProductBasket);
        imageButtonBasket.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
                int userID = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));
                try{
                    String query = "select * from ShoppingCart where shoppingCartState='0' and shoppingCartUserID='"+userID+"'";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    if (resultSet.next() == false) {
                        Snackbar.make(view,"Sepet boş",Snackbar.LENGTH_SHORT).show();
                    }else{
                        Intent intent = new Intent(ProductActivity.this,CartActivity.class);
                        startActivity(intent);
                    }

                }catch (Exception e){
                    System.out.println("Exception"+e);
                }
            }
        });

        arrayListProduct = new ArrayList<>();
        myTask();
        progressDialog = ProgressDialog.show(this,"","Loading",true);
    }

    private void Search(String productName){
        ArrayList<Product> filteredArrayList = new ArrayList<>();

        for(Product product : arrayListProduct){
            if(product.getProductName().toLowerCase().contains(productName.toLowerCase())){
                filteredArrayList.add(product);
            }
            if (filteredArrayList.isEmpty()) {
                filteredArrayList.clear();
                adapter.filterList(filteredArrayList);
            } else {
                adapter.filterList(filteredArrayList);
            }

        }
    }

    public void myTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection == null){
                        check = false;
                    }else{
                        String query = "select * from Product where productCategoryID='" +categoryID+ "'";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if(resultSet != null){
                            while (resultSet.next()){
                                arrayListProduct.add(new Product(resultSet.getInt("productID"),resultSet.getString("productName"),resultSet.getString("productImage"),resultSet.getFloat("productWeight")));
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
                                adapter = new CustomAdapter(arrayListProduct);
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
        private ArrayList<Product> productItemLists;
        public class ViewHolder extends RecyclerView.ViewHolder{
            private final TextView productName;
            private final TextView productWeight;
            private final ImageView productImage;
            public ViewHolder(View view){
                super(view);
                productName = view.findViewById(R.id.textViewToDoDetailName);
                productImage = view.findViewById(R.id.imageViewToDoDetail);
                productWeight = view.findViewById(R.id.textViewToDoDetailWeight);
            }
        }
        public CustomAdapter(ArrayList<Product> itemLists){
            productItemLists = itemLists;
        }

        public void filterList(ArrayList<Product> filteredlist) {
            this.productItemLists = filteredlist;

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final Product product = productItemLists.get(position);
            holder.productName.setText(product.getProductName());
            Picasso.get().load(product.getProductImage()).fit().centerCrop().into(holder.productImage);
            holder.productWeight.setText(product.getProductWeight().toString()+" g");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(fragmentAddToCart.isAdded()){
                        ProductActivity.this.getFragmentManager().popBackStack();
                        ProductActivity.this.onBackPressed();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.addToBackStack(null);
                        Bundle bundle = new Bundle();
                        bundle.putInt("productID",product.getProductID());
                        bundle.putString("productName",product.getProductName());
                        bundle.putString("productImage",product.getProductImage());
                        bundle.putFloat("productWeight",product.getProductWeight());
                        fragmentAddToCart.setArguments(bundle);
                        fragmentTransaction.add(R.id.frameLayaut,fragmentAddToCart);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        frameLayout.setVisibility(View.VISIBLE);
                        return;
                    }

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    Bundle bundle = new Bundle();
                    bundle.putInt("productID",product.getProductID());
                    bundle.putString("productName",product.getProductName());
                    bundle.putString("productImage",product.getProductImage());
                    bundle.putFloat("productWeight",product.getProductWeight());
                    fragmentAddToCart.setArguments(bundle);
                    fragmentTransaction.add(R.id.frameLayaut,fragmentAddToCart);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                    frameLayout.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return productItemLists.size();
        }
    }
}