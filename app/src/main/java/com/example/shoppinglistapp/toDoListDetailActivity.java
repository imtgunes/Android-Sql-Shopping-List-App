package com.example.shoppinglistapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class toDoListDetailActivity extends AppCompatActivity {
    private int todoID;
    private ConnectionClass connectionClass;
    private Connection connection;

    private ArrayList<Cart> arrayList;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private boolean check = false;
    private ProgressDialog progressDialog;

    private TextView textViewToDoDetailToDoNote,textViewToDoDetailToDoNoteHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_list_detail);

        getWindow().setStatusBarColor(getResources().getColor(R.color.mainBackground));

        todoID = getIntent().getIntExtra("todoID",0);

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();
        recyclerView = findViewById(R.id.recyclerViewToDoDetail);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        textViewToDoDetailToDoNote = findViewById(R.id.textViewToDoDetailToDoNote);
        textViewToDoDetailToDoNoteHead = findViewById(R.id.textView3);

        try {
            String query = "select * from ToDoList t,ShoppingCart sc,ShoppingCarts s,Product p where t.todoShoppingCartID = sc.shoppingCartID and s.shoppingCartsCartID=sc.shoppingCartID and s.shoppingCartsProductID = p.productID and t.todoID='"+todoID+"'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet != null){
                while (resultSet.next()){
                    if (TextUtils.isEmpty(resultSet.getString("shoppingCartNote") )) {
                        textViewToDoDetailToDoNote.setVisibility(View.GONE);
                        textViewToDoDetailToDoNoteHead.setVisibility(View.GONE);
                    }
                    else {
                        textViewToDoDetailToDoNote.setText(resultSet.getString("shoppingCartNote"));
                        textViewToDoDetailToDoNote.setPadding(20, 20, 20, 20);
                    }
                }
            }

        }catch (Exception e){

        }

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
                        String query = "select * from ToDoList t,ShoppingCart sc,ShoppingCarts s,Product p where t.todoShoppingCartID = sc.shoppingCartID and s.shoppingCartsCartID=sc.shoppingCartID and s.shoppingCartsProductID = p.productID and t.todoID='"+todoID+"'";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if(resultSet != null){
                            while (resultSet.next()){
                                if (TextUtils.isEmpty(resultSet.getString("shoppingCartNote") )) {
                                    textViewToDoDetailToDoNote.setVisibility(View.GONE);
                                    textViewToDoDetailToDoNoteHead.setVisibility(View.GONE);
                                }
                                else {
                                    textViewToDoDetailToDoNote.setText(resultSet.getString("shoppingCartNote"));
                                    textViewToDoDetailToDoNote.setPadding(20, 20, 20, 20);
                                }

                                arrayList.add(new Cart(resultSet.getInt("productID"), resultSet.getString("productName"), resultSet.getInt("productCategoryID"), resultSet.getString("productImage"), resultSet.getFloat("productWeight"), resultSet.getInt("shoppingCartsPiece"), resultSet.getString("shoppingCartsNote")));
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
        private ArrayList<Cart> cartItemLists;
        public class ViewHolder extends RecyclerView.ViewHolder{
            private final TextView productName;
            private final TextView productWeight;
            private final TextView productPiece;
            private final TextView productNote;
            private final ImageView productImage;
            public ViewHolder(View view){
                super(view);
                productName = view.findViewById(R.id.textViewToDoDetailName);
                productImage = view.findViewById(R.id.imageViewToDoDetail);
                productWeight = view.findViewById(R.id.textViewToDoDetailWeight);
                productPiece = view.findViewById(R.id.textViewToDoDetailPiece);
                productNote = view.findViewById(R.id.textViewToDoDetailNote);
            }
        }
        public CustomAdapter(ArrayList<Cart> itemLists){
            cartItemLists = itemLists;
        }
        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_todo_detail,parent,false);
            return new CustomAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.ViewHolder holder, int position) {
            final Cart cart = cartItemLists.get(position);
            holder.productName.setText(cart.getProductName());
            holder.productPiece.setText("Adet "+String.valueOf(cart.getShoppingCartPiece()));
            holder.productNote.setText(cart.getShoppingCartNote());
            Picasso.get().load(cart.getProductImage()).fit().centerCrop().into(holder.productImage);
            holder.productWeight.setText(cart.getProductWeight().toString()+" g");

        }

        @Override
        public int getItemCount() {
            return cartItemLists.size();
        }
    }

}