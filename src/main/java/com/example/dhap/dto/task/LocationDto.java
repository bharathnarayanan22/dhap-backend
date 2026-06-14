package com.example.dhap.dto.task;

import com.example.dhap.entities.Location;

/** Used both in CreateTaskRequest (input) and TaskResponse (output). */
public class LocationDto {
    public Double latitude;
    public Double longitude;

    public LocationDto() {}

    public Location toEntity() {
        return new Location(latitude, longitude);
    }

    public static LocationDto from(Location location) {
        if (location == null) return null;
        LocationDto dto = new LocationDto();
        dto.latitude  = location.latitude;
        dto.longitude = location.longitude;
        return dto;
    }
}