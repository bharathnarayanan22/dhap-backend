package com.example.dhap.services;

import com.example.dhap.dto.dashboard.DashboardStatsResponse;
import com.example.dhap.enums.TaskStatus;
import com.example.dhap.repositories.RequestRepository;
import com.example.dhap.repositories.ResourceRepository;
import com.example.dhap.repositories.TaskRepository;
import com.example.dhap.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final TaskRepository     taskRepository;
    private final UserRepository     userRepository;
    private final RequestRepository  requestRepository;
    private final ResourceRepository resourceRepository;

    public DashboardService(TaskRepository taskRepository,
                            UserRepository userRepository,
                            RequestRepository requestRepository,
                            ResourceRepository resourceRepository) {
        this.taskRepository     = taskRepository;
        this.userRepository     = userRepository;
        this.requestRepository  = requestRepository;
        this.resourceRepository = resourceRepository;
    }

    public DashboardStatsResponse getStats() {
        DashboardStatsResponse res = new DashboardStatsResponse();

        res.tasks.total          = taskRepository.count();
        res.tasks.pending        = taskRepository.countByStatus(TaskStatus.PENDING);
        res.tasks.inProgress     = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        res.tasks.inVerification = taskRepository.countByStatus(TaskStatus.IN_VERIFICATION);
        res.tasks.completed      = taskRepository.countByStatus(TaskStatus.COMPLETED);

        res.volunteers.active = userRepository.countByInTask(true);
        res.volunteers.total  = userRepository.countByRole("VOLUNTEER");

        res.requests.total    = requestRepository.count();
        res.requests.pending  = requestRepository.countByStatus("PENDING");
        res.requests.accepted = requestRepository.countByStatus("ACCEPTED");

        res.resources.total    = resourceRepository.count();
        res.resources.food     = resourceRepository.countByResourceType("FOOD");
        res.resources.water    = resourceRepository.countByResourceType("WATER");
        res.resources.medicine = resourceRepository.countByResourceType("MEDICINE");
        res.resources.shelter  = resourceRepository.countByResourceType("SHELTER");

        return res;
    }
}
