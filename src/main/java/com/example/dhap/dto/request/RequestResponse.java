package com.example.dhap.dto.request;

import com.example.dhap.dto.task.LocationDto;
import com.example.dhap.entities.Request;

/** Returned by GET /requests and POST /requests. */
public class RequestResponse {

    public String id;
    public String resource;
    public int    quantity;
    public String description;
    public String address;
    public LocationDto location;
    public String status;           // "PENDING" or "ACCEPTED"
    public int    quantityPledged;

    public RequestResponse() {}

    public static RequestResponse from(Request r) {
        RequestResponse dto  = new RequestResponse();
        dto.id               = r.getId();
        dto.resource         = r.getResource();
        dto.quantity         = r.getQuantity();
        dto.description      = r.getDescription();
        dto.address          = r.getAddress();
        dto.location         = LocationDto.from(r.getLocation());
        dto.status           = r.getStatus();
        dto.quantityPledged  = r.getQuantityPledged();
        return dto;
    }
}