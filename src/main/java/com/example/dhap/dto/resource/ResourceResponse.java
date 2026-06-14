package com.example.dhap.dto.resource;

import com.example.dhap.dto.task.LocationDto;
import com.example.dhap.entities.Resource;

/** Returned by GET /resources and POST /resources. */
public class ResourceResponse {

    public String id;
    public String resource;
    public int    quantity;
    public String address;
    public LocationDto location;
    public String donorId;
    public String donorName;
    public String resourceType;

    public ResourceResponse() {}

    public static ResourceResponse from(Resource r) {
        ResourceResponse dto  = new ResourceResponse();
        dto.id           = r.getId();
        dto.resource     = r.getResource();
        dto.quantity     = r.getQuantity();
        dto.address      = r.getAddress();
        dto.location     = LocationDto.from(r.getLocation());
        dto.donorId      = r.getDonorId();
        dto.donorName    = r.getDonorName();
        dto.resourceType = r.getResourceType();
        return dto;
    }
}