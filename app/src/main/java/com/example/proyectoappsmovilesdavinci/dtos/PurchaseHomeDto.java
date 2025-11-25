package com.example.proyectoappsmovilesdavinci.dtos;

public class PurchaseHomeDto {

    private int id;
    private double amount;
    private String name;
    private int financialEntityId;
    private String imageUri;

    public PurchaseHomeDto(
            int id,
            double amount,
            String name,
            int financialEntityId
    ) {
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.financialEntityId = financialEntityId;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getName() { return name; }
    public int getFinancialEntityId() { return financialEntityId; }
    public String getImageUri() { return imageUri; }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
