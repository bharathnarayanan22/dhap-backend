package com.example.dhap.dto.task;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** Body for POST /tasks — coordinator creates a task. */
public class CreateTaskRequest {

    @NotBlank(message = "title is required")
    public String title;

    public String description;

    /** How many volunteers are needed. */
    @Min(value = 1, message = "volunteer must be at least 1")
    public int volunteer;

    public String startAddress;
    public String endAddress;

    public LocationDto startLocation;
    public LocationDto endLocation;
}