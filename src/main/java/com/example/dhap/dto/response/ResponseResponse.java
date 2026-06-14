package com.example.dhap.dto.response;

import com.example.dhap.dto.task.LocationDto;
import com.example.dhap.entities.Response;

/** Returned by GET /responses, POST /responses, and PATCH /responses/{id}/assign-task. */
public class ResponseResponse {

    public String  id;
    public String  requestId;
    public String  responderId;
    public String  responderName;
    public String  message;
    public int     quantityProvided;
    public String  address;
    public LocationDto location;
    public boolean taskAssigned;

    public ResponseResponse() {}

    public static ResponseResponse from(Response r) {
        ResponseResponse dto  = new ResponseResponse();
        dto.id               = r.getId();
        dto.requestId        = r.getRequestId();
        dto.responderId      = r.getResponderId();
        dto.responderName    = r.getResponderName();
        dto.message          = r.getMessage();
        dto.quantityProvided = r.getQuantityProvided();
        dto.address          = r.getAddress();
        dto.location         = LocationDto.from(r.getLocation());
        dto.taskAssigned     = r.isTaskAssigned();
        return dto;
    }
}