package com.example.dhap.dto.user;

import com.example.dhap.entities.User;

/**
 * Read-only user profile returned by GET /users and GET /users/{id}.
 * Password and refreshToken are never included.
 */
public class UserResponse {
    public String  id;
    public String  name;
    public String  email;
    public String  mobile;
    public String  addressLine;
    public String  city;
    public String  country;
    public String  pincode;
    public String  role;         // DONOR | VOLUNTEER | COORDINATOR
    public boolean inTask;
    public boolean isCoordinator;
    public boolean isSubmitted;

    public static UserResponse from(User user) {
        UserResponse r  = new UserResponse();
        r.id            = user.getId();
        r.name          = user.getName();
        r.email         = user.getEmail();
        r.mobile        = user.getMobile();
        r.addressLine   = user.getAddressLine();
        r.city          = user.getCity();
        r.country       = user.getCountry();
        r.pincode       = user.getPincode();
        r.role          = user.getRole();
        r.inTask        = user.isInTask();
        r.isCoordinator = user.isCoordinator() || "COORDINATOR".equals(user.getRole());
        r.isSubmitted   = Boolean.TRUE.equals(user.getIsSubmitted());
        return r;
    }
}