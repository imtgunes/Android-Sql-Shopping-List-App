package com.example.shoppinglistapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {
    private ConnectionClass connectionClass;
    private Connection connection;

    private ArrayList<Cart> arrayList;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private boolean check = false;
    private ProgressDialog progressDialog;

    private Button saveToDoList,showNote;
    private String toDoNote = "";
    private TextView textViewCartEmpty;

    private LocalDateTime timePoint;

    private int userID;

    private SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getWindow().setStatusBarColor(getResources().getColor(R.color.topLayoutBackground));

        sharedPreferences = getSharedPreferences("userNo",MODE_PRIVATE);
        userID = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));

        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        recyclerView = findViewById(R.id.recyclerViewCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        showNote = findViewById(R.id.buttonNote);
        showNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(CartActivity.this);
                bottomSheetDialog.setContentView(R.layout.bottom_dialog);
                bottomSheetDialog.setCanceledOnTouchOutside(true);
                EditText editTextCartNoteList= bottomSheetDialog.findViewById(R.id.editTextCartNoteList);
                ImageButton imageButtonExitBottom = bottomSheetDialog.findViewById(R.id.imageButtonExitBottom);
                imageButtonExitBottom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetDialog.cancel();
                    }
                });
                Button buttonAddToCartNoteBottom = bottomSheetDialog.findViewById(R.id.buttonAddToCartNoteBottom);
                buttonAddToCartNoteBottom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        toDoNote = String.valueOf(editTextCartNoteList.getText());
                        bottomSheetDialog.cancel();
                    }
                });

                if(bottomSheetDialog.getWindow() != null)
                    bottomSheetDialog.getWindow().setDimAmount(0);
                bottomSheetDialog.show();
            }
        });

        textViewCartEmpty = findViewById(R.id.textViewCartEmpty);
        textViewCartEmpty.setVisibility(View.GONE);

        saveToDoList = findViewById(R.id.buttonSaveToDo);
        saveToDoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    timePoint = LocalDateTime.now(
                    );
                }
                try{
                    String query1 = "select * from ShoppingCarts s,Product p, ShoppingCart sc where p.productID=s.shoppingCartsProductID  and s.shoppingCartsCartID = sc.shoppingCartID and sc.shoppingCartUserID = '"+userID+"'";
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query1);
                    if (resultSet.next() == false) {
                        Snackbar.make(view,"Sepet boş",Snackbar.LENGTH_SHORT).show();
                    }else{
                        String query = "update ShoppingCart set shoppingCartNote='"+toDoNote+"' where shoppingCartID=(select shoppingCartID from ShoppingCart where shoppingCartState='0'and shoppingCartUserID='"+userID+"' )" +
                                "Declare @id INT\n" +
                                "select DISTINCT @id=shoppingCartID from ShoppingCarts s,Product p, ShoppingCart sc where p.productID=s.shoppingCartsProductID  and s.shoppingCartsCartID = sc.shoppingCartID and sc.shoppingCartUserID = '"+userID+"'\n" +
                                "IF NOT EXISTS (select * from ToDoList where todoShoppingCartID=@id)\n" +
                                "\tBEGIN\t\n" +
                                "\t\tinsert into ToDoList (todoShoppingCartID,todoSaveDate,todoUserID,todoState) values (@id, '"+timePoint+"', '"+userID+"', '0')\n" +
                                "\t    update ShoppingCart set shoppingCartState='1' where shoppingCartID=@id\n" +
                                "    END ";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();
                        Snackbar.make(view,"Liste kaydedildi",Snackbar.LENGTH_SHORT).show();
                        thread.start();
                    }

                }catch (Exception e){
                    System.out.println("Exception"+e);
                }

            }
        });

        arrayList = new ArrayList<>();
        myTask();
        progressDialog = ProgressDialog.show(this,"","Yükleniyor",true);
    }

    Thread thread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                CartActivity.this.finish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public void myTask(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection == null){
                        check = false;
                    }else{
                        String query = "select * from Product p,ShoppingCarts s,ShoppingCart sc where s.shoppingCartsProductID = p.productID and s.shoppingCartsCartID=sc.shoppingCartID and sc.shoppingCartState='0' and sc.shoppingCartUserID='"+userID+"'";
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        if(resultSet != null){
                            while (resultSet.next()){
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
                        if(check == false){
                            System.out.println("An error occurred while retrieving data");
                        }
                        else {
                            progressDialog.dismiss();
                            try {
                                adapter = new CustomAdapter(arrayList);
                                recyclerView.setAdapter(adapter);
                                if(arrayList.isEmpty()){
                                    textViewCartEmpty.setVisibility(View.VISIBLE);
                                    textViewCartEmpty.setText("Sepetiniz boş lütfen ürün ekleyiniz");
                                }
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
                productName = view.findViewById(R.id.textViewCartName);
                productImage = view.findViewById(R.id.imageViewCart);
                productWeight = view.findViewById(R.id.textViewCartlWeight);
                productPiece = view.findViewById(R.id.textViewCartPiece);
                productNote = view.findViewById(R.id.textViewCartNote);
            }
        }
        public CustomAdapter(ArrayList<Cart> itemLists){
            cartItemLists = itemLists;
        }
        @NonNull
        @Override
        public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_cart,parent,false);
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
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                    builder.setMessage("Ürünü sepetten kaldırıyorsunuz");
                    builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                String query = "delete from ShoppingCarts where shoppingCartsProductID='" +cart.getProductID()+"'";
                                PreparedStatement preparedStatement = connection.prepareStatement(query);
                                preparedStatement.executeUpdate();
                                arrayList.clear();
                                myTask();
                                Snackbar.make(view,"Ürün silindi",Snackbar.LENGTH_SHORT).show();

                            } catch(Exception e) {
                                System.out.println(e);
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
        }

        @Override
        public int getItemCount() {
            return cartItemLists.size();
        }
    }
}