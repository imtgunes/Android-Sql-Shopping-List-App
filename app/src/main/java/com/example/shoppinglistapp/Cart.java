package com.example.shoppinglistapp;

public class Cart {
    private int productID;
    private String productName;
    private int productCategoryID;
    private String productImage;
    private Float productWeight;
    private int shoppingCartPiece;
    private String shoppingCartNote;

    public Cart(int productID, String productName, int productCategory, String productImage, float productWeight, int shoppingCartPiece, String shoppingCartNote) {
        this.productID = productID;
        this.productName = productName;
        this.productCategoryID = productCategory;
        this.productImage = productImage;
        this.productWeight = productWeight;
        this.shoppingCartPiece = shoppingCartPiece;
        this.shoppingCartNote = shoppingCartNote;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Float getProductWeight() {
        return productWeight;
    }

    public void setProductWeight(Float productWeight) {
        this.productWeight = productWeight;
    }

    public int getShoppingCartPiece() {
        return shoppingCartPiece;
    }

    public void setShoppingCartPiece(int shoppingCartPiece) {
        this.shoppingCartPiece = shoppingCartPiece;
    }

    public String getShoppingCartNote() {
        return shoppingCartNote;
    }

    public void setShoppingCartNote(String shoppingCartNote) {
        this.shoppingCartNote = shoppingCartNote;
    }

    public int getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(int productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

}
