package com.example.dhap.dto.task;

import com.example.dhap.entities.Task;
import com.example.dhap.enums.TaskStatus;

import java.util.List;

/** Read-only task returned by all /tasks endpoints. */
public class TaskResponse {
    public String id;
    public String title;
    public String description;

    public int volunteer;
    public int volunteersAccepted;

    public String      startAddress;
    public String      endAddress;
    public LocationDto startLocation;
    public LocationDto endLocation;

    public TaskStatus status;

    public List<String>   assignedUserIds;
    public List<ProofDto> proofs;

    public static TaskResponse from(Task task) {
        TaskResponse r       = new TaskResponse();
        r.id                 = task.id;
        r.title              = task.title;
        r.description        = task.description;
        r.volunteer          = task.volunteer;
        r.volunteersAccepted = task.volunteersAccepted;
        r.startAddress       = task.startAddress;
        r.endAddress         = task.endAddress;
        r.startLocation      = LocationDto.from(task.startLocation);
        r.endLocation        = LocationDto.from(task.endLocation);
        r.status             = task.status;
        r.assignedUserIds    = task.assignedUserIds;
        r.proofs             = task.proofs.stream().map(ProofDto::from).toList();
        return r;
    }
}