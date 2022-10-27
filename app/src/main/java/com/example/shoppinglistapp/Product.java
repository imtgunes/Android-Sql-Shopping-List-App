package com.example.shoppinglistapp;

public class Product {
    private int productID;
    private String productName;
    private String productCategory;
    private String productImage;
    private Float productWeight;

    public Product(int productID, String productName, String productImage, float productWeight) {
        this.productID = productID;
        this.productName = productName;
        this.productImage = productImage;
        this.productWeight = productWeight;
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

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
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

}

