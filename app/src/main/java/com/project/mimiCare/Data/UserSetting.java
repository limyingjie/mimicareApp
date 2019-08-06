package com.project.mimiCare.Data;

public class UserSetting {
    private String name;
    private String email;
    private String phoneNumber;
    private String dob;
    private String profile_photo_uri;

    public UserSetting(String name, String email, String phoneNumber, String dob, String profile_photo_uri) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.profile_photo_uri = profile_photo_uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getProfile_photo_uri() {
        return profile_photo_uri;
    }

    public void setProfile_photo_uri(String profile_photo_uri) {
        this.profile_photo_uri = profile_photo_uri;
    }
}
