package com.example.serviceco;

public class MyItems {

    private final String name, role,rating, location, img, phone, about;


    public MyItems(String name, String role, String rating, String location, String img, String phone, String about) {
        this.name = name;
        this.role = role;
        this.rating = rating;
        this.location=location;
        this.img = img;
        this.phone = phone;
        this.about = about;
    }

    public String getPhone(){
        return phone;
    }

    public String getAbout(){
        return about;
    }

    public String getImg(){
        return img;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getRating() {
        return rating;
    }

    public String getLocation(){return location; }
}