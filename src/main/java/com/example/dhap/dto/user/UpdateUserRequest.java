package com.example.dhap.dto.user;

/**
 * Body for PATCH /users/{id} — all fields are optional (partial update).
 * Only non-null values are applied.
 */
public class UpdateUserRequest {
    public String name;
    public String mobile;
    public String addressLine;
    public String city;
    public String country;
    public String pincode;
}