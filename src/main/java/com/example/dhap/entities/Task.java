package com.example.dhap.entities;

import com.example.dhap.enums.TaskStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Document(collection = "tasks")
public class Task {

    @Id
    public String id = UUID.randomUUID().toString();

    public String title;
    public String description;

    /** How many volunteers are needed. */
    public int volunteer;

    /** How many have accepted so far — incremented atomically on accept. */
    public int volunteersAccepted = 0;

    public String   startAddress;
    public String   endAddress;
    public Location startLocation;
    public Location endLocation;

    public TaskStatus status = TaskStatus.PENDING;

    /** IDs of volunteers who accepted. Used for capacity check and COMPLETED cascade. */
    public List<String> assignedUserIds = new ArrayList<>();

    /** Proof submissions — embedded, not a separate collection. */
    public List<Proof> proofs = new ArrayList<>();

    public Task() {}
}