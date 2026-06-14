package com.example.dhap.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

/**
 * A coordinator's published resource need — stored in "requests" collection.
 *
 * v2 design note: no responseIds[] list.
 * Responses reference their request via responderId field and are queried
 * with ResponseRepository.findByRequestId().
 *
 * quantityPledged is maintained in-place: each POST /responses increments it
 * inside a @Transactional block and flips status to ACCEPTED when filled.
 */
@Document(collection = "requests")
public class Request {

    @Id
    private String id = UUID.randomUUID().toString();

    private String   resource;
    private int      quantity;         // total needed
    private String   description;
    private String   address;
    private Location location;

    /** Stored as RequestStatus enum name: "PENDING" or "ACCEPTED". */
    private String status = "PENDING";

    /** Running total pledged by donors — incremented atomically on each pledge. */
    private int quantityPledged = 0;

    public Request() {}

    // ── Getters ────────────────────────────────────────────────────────────
    public String   getId()              { return id; }
    public String   getResource()        { return resource; }
    public int      getQuantity()        { return quantity; }
    public String   getDescription()     { return description; }
    public String   getAddress()         { return address; }
    public Location getLocation()        { return location; }
    public String   getStatus()          { return status; }
    public int      getQuantityPledged() { return quantityPledged; }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setResource(String resource)           { this.resource = resource; }
    public void setQuantity(int quantity)              { this.quantity = quantity; }
    public void setDescription(String description)     { this.description = description; }
    public void setAddress(String address)             { this.address = address; }
    public void setLocation(Location location)         { this.location = location; }
    public void setStatus(String status)               { this.status = status; }
    public void setQuantityPledged(int quantityPledged){ this.quantityPledged = quantityPledged; }
}