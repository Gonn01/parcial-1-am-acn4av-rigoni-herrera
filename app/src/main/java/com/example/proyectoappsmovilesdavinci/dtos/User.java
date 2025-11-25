package com.example.proyectoappsmovilesdavinci.dtos;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String email;
    private String name;

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
    }
    public String getName() {
        return name;
    }
}
