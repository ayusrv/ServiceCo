package com.example.serviceco;

public class ReadWriteUserDetails {
    public String fullName, email, location, mobile;

    public ReadWriteUserDetails(){};

    public ReadWriteUserDetails(String fullName, String email, String location, String mobile){
        this.fullName = fullName;
        this.email = email;
        this.location = location;
        this.mobile = mobile;
    }

    public ReadWriteUserDetails(String location, String mobile) {
        this.location = location;
        this.mobile = mobile;
    }
}
