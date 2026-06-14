package com.example.dhap.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.UUID;

/**
 * A donor's pledge against a resource request — stored in "responses" collection.
 *
 * responderId / responderName: set from JWT at creation time, never from request body.
 * taskAssigned: flipped to true by PATCH /responses/{id}/assign-task when a coordinator
 *               converts this pledge into a delivery task.
 */
@Document(collection = "responses")
public class Response {

    @Id
    private String id = UUID.randomUUID().toString();

    private String  requestId;        // references Request._id
    private String  responderId;      // User.id of the donor
    private String  responderName;    // User.name snapshot (no join needed on reads)
    private String  message;
    private int     quantityProvided;
    private String  address;
    private Location location;
    private boolean taskAssigned = false;

    public Response() {}

    // ── Getters ────────────────────────────────────────────────────────────
    public String   getId()              { return id; }
    public String   getRequestId()       { return requestId; }
    public String   getResponderId()     { return responderId; }
    public String   getResponderName()   { return responderName; }
    public String   getMessage()         { return message; }
    public int      getQuantityProvided(){ return quantityProvided; }
    public String   getAddress()         { return address; }
    public Location getLocation()        { return location; }
    public boolean  isTaskAssigned()     { return taskAssigned; }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setRequestId(String requestId)           { this.requestId = requestId; }
    public void setResponderId(String responderId)       { this.responderId = responderId; }
    public void setResponderName(String responderName)   { this.responderName = responderName; }
    public void setMessage(String message)               { this.message = message; }
    public void setQuantityProvided(int quantityProvided){ this.quantityProvided = quantityProvided; }
    public void setAddress(String address)               { this.address = address; }
    public void setLocation(Location location)           { this.location = location; }
    public void setTaskAssigned(boolean taskAssigned)    { this.taskAssigned = taskAssigned; }
}