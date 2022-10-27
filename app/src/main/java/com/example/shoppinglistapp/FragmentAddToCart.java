package com.example.shoppinglistapp;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FragmentAddToCart extends Fragment {
    private ImageButton imageButtonExit;
    private Button buttonAddToCart;
    private ImageView imageViewPiecePlus,imageViewPieceMinus,imageViewProductImage;
    private TextView textViewPieceFragment,textViewProductNameFragment,textViewProductWeight;
    private EditText editTextNote;
    private int productPiece = 1;
    private String cartNote = "";

    private ConnectionClass connectionClass;
    private Connection connection;

    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_to_cart, container, false);
        int productID = this.getArguments().getInt("productID",0);
        String productName = this.getArguments().getString("productName");
        String prodcutImage = this.getArguments().getString("productImage");
        Float productWeight = this.getArguments().getFloat("productWeight");
        //Toast.makeText(getContext(),"id = "+productID,Toast.LENGTH_SHORT).show();
        connectionClass = new ConnectionClass();
        connection = connectionClass.connection();

        imageButtonExit = view.findViewById(R.id.imageButtonExit);
        imageViewPiecePlus = view.findViewById(R.id.imageViewPlusFragment);
        imageViewPieceMinus = view.findViewById(R.id.imageViewMinusFragment);
        textViewPieceFragment = view.findViewById(R.id.textViewProductPieceFragment);
        textViewProductNameFragment = view.findViewById(R.id.textViewProductNameFragment);
        imageViewProductImage = view.findViewById(R.id.imageViewToDoDetail);
        buttonAddToCart = view.findViewById(R.id.buttonAddToCartFragment);
        textViewProductWeight = view.findViewById(R.id.textViewProductWeightFragment);
        editTextNote = view.findViewById(R.id.editTextTextNoteFragment);

        textViewProductNameFragment.setText(productName);
        textViewProductWeight.setText(String.valueOf(productWeight)+" g");

        Picasso.get().load(prodcutImage).into(imageViewProductImage);
        Activity activity = getActivity();

        buttonAddToCart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                sharedPreferences = getActivity().getSharedPreferences("userNo",MODE_PRIVATE);
                int userID = Integer.parseInt(Encryption.decrypt(sharedPreferences.getString("userNo","GuZMgQ2zRFt6sFV53NLtnA==").toString()));
                cartNote = String.valueOf(editTextNote.getText());
                try {
                    String query = "IF EXISTS (select shoppingCartID from ShoppingCart where shoppingCartState='0' and shoppingCartUserID='"+userID+"')\n" +
                            "\n" +
                            "\tBEGIN\n" +
                            "\t\tDECLARE @id INT\n" +
                            "\t\tselect @id=shoppingCartID from ShoppingCart where shoppingCartState='0' and shoppingCartUserID='"+userID+"'\n" +
                            "\t\tIF EXISTS (SELECT * FROM ShoppingCarts WHERE shoppingCartsProductID = '" +productID+ "' and shoppingCartsCartID=@id)\n" +
                            "\t\tBEGIN\n" +
                            "\t\t\tupdate ShoppingCarts set shoppingCartsPiece = shoppingCartsPiece+'" +productPiece+ "' where shoppingCartsProductID='" +productID+ "'\n" +
                            "\t\t\tupdate ShoppingCarts set shoppingCartsNote = '" +cartNote+ "' where shoppingCartsProductID='" +productID+ "'\n" +
                            "\t\tEND \n" +
                            "\n" +
                            "\t\tELSE    \n" +
                            "\t\tBEGIN\n" +
                            "\t\t\tinsert into ShoppingCarts (shoppingCartsCartID,shoppingCartsProductID,shoppingCartsPiece,shoppingCartsNote) values (@id, '" +productID+ "', '" +productPiece+ "', '" +editTextNote.getText()+"')\n" +
                            "\t\tEND\n" +
                            "\t\t\n" +
                            "    END \n" +
                            "\n" +
                            "ELSE    \n" +
                            "     BEGIN\n" +
                            "       insert into ShoppingCart (shoppingCartState,shoppingCartUserID) values ('0', '"+userID+"')\n" +
                            "\t   DECLARE @id1 INT\n" +
                            "\t\tselect @id1=shoppingCartID from ShoppingCart where shoppingCartState='0' and shoppingCartUserID='"+userID+"'\n" +
                            "\t   insert into ShoppingCarts (shoppingCartsCartID,shoppingCartsProductID,shoppingCartsPiece,shoppingCartsNote) values (@id1, '" +productID+ "', '" +productPiece+ "', '" +editTextNote.getText()+"')\n" +
                            "     END";

                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    Snackbar snackbar = Snackbar.make(view,"Ürün eklendi ",Snackbar.LENGTH_SHORT)
                            .setAction("Sepete Git", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(activity, CartActivity.class);
                                    activity.startActivity(intent);
                                }
                            });
                    snackbar.show();
                }catch (Exception e){
                    System.out.println("Exception"+e);
                }

                editTextNote.setText("");
                getActivity().getFragmentManager().popBackStack();
                hideSoftKeyboard(getActivity());
                getActivity().onBackPressed();
            }
        });

        imageViewPiecePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productPiece = Integer.parseInt(textViewPieceFragment.getText().toString());

                productPiece++;
                textViewPieceFragment.setText(String.valueOf(productPiece));

            }
        });

        imageViewPieceMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productPiece = Integer.parseInt(textViewPieceFragment.getText().toString());
                if(productPiece > 1){
                    productPiece--;
                    textViewPieceFragment.setText(String.valueOf(productPiece));
                }


            }
        });

        imageButtonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextNote.setText("");
                hideSoftKeyboard(getActivity());
                getActivity().getFragmentManager().popBackStack();
                getActivity().onBackPressed();

            }
        });

        return view;
    }
    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}