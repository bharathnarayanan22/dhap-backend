package com.example.dhap.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "users")
public class User {

    @Id
    private String id = UUID.randomUUID().toString();

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String mobile;

    private String addressLine;

    private String city;

    private String country;

    private String pincode;

    private String role; // VOLUNTEER / DONOR / COORDINATOR

    private Boolean isSubmitted;

    /** True once a coordinator application has been accepted (set in ApplicationService.accept). */
    private boolean isCoordinator = false;

    /** True while the user has at least one accepted, uncompleted task. */
    private boolean inTask = false;

    public User() {
    }

    public User(String id, String name, String email, String password, String mobile,
                String addressLine, String city, String country, String pincode,
                String role, Boolean isSubmitted) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.addressLine = addressLine;
        this.city = city;
        this.country = country;
        this.pincode = pincode;
        this.role = role;
        this.isSubmitted = isSubmitted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsSubmitted() {
        return isSubmitted;
    }

    public void setIsSubmitted(Boolean isSubmitted) {
        this.isSubmitted = isSubmitted;
    }

    public boolean isCoordinator() {
        return isCoordinator;
    }

    public void setCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    public boolean isInTask() {
        return inTask;
    }

    public void setInTask(boolean inTask) {
        this.inTask = inTask;
    }
}