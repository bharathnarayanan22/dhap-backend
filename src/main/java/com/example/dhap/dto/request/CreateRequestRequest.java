package com.example.dhap.dto.request;

import com.example.dhap.dto.task.LocationDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Body for POST /requests — coordinator publishes a resource need. */
public class CreateRequestRequest {

    @NotBlank(message = "resource name is required")
    public String resource;

    @Min(value = 1, message = "quantity must be at least 1")
    public int quantity;

    public String      description;
    public String      address;
    public LocationDto location;
}