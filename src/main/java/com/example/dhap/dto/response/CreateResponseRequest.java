package com.example.dhap.dto.response;

import com.example.dhap.dto.task.LocationDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Body for POST /responses — donor pledges against a resource request.
 * responderId and responderName are resolved from the JWT, not sent by the client.
 */
public class CreateResponseRequest {

    @NotBlank(message = "requestId is required")
    public String requestId;

    public String message;

    @Min(value = 1, message = "quantityProvided must be at least 1")
    public int quantityProvided;

    public String      address;
    public LocationDto location;
}