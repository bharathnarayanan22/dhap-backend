package com.example.dhap.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

@Document(collection = "resources")
public class Resource {

    @Id
    private String id = UUID.randomUUID().toString();

    private String resource;     // resource name / description
    private int    quantity;
    private String address;
    private Location location;   // embedded — same Location used by Task

    private String donorId;      // User.id of the donor — set from JWT, never from request body
    private String donorName;    // User.name snapshot — stored so reads need no join

    /** Stored as the ResourceType enum name: "FOOD", "MEDICAL", etc. */
    private String resourceType;

    public Resource() {}

    // ── Getters ────────────────────────────────────────────────────────────
    public String getId()           { return id; }
    public String getResource()     { return resource; }
    public int    getQuantity()     { return quantity; }
    public String getAddress()      { return address; }
    public Location getLocation()   { return location; }
    public String getDonorId()      { return donorId; }
    public String getDonorName()    { return donorName; }
    public String getResourceType() { return resourceType; }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setResource(String resource)         { this.resource = resource; }
    public void setQuantity(int quantity)            { this.quantity = quantity; }
    public void setAddress(String address)           { this.address = address; }
    public void setLocation(Location location)       { this.location = location; }
    public void setDonorId(String donorId)           { this.donorId = donorId; }
    public void setDonorName(String donorName)       { this.donorName = donorName; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
}