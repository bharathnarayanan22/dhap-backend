package com.example.dhap.dto.resource;

import com.example.dhap.dto.task.LocationDto;
import com.example.dhap.enums.ResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Body for POST /resources.
 * Donor identity (donorId, donorName) is resolved from the JWT — not in this body.
 */
public class CreateResourceRequest {

    @NotBlank(message = "resource name is required")
    public String resource;

    @Min(value = 1, message = "quantity must be at least 1")
    public int quantity;

    public String      address;
    public LocationDto location;

    /** Validated against the ResourceType enum; stored as its name String in MongoDB. */
    public ResourceType resourceType;
}