package com.example.dhap.services;

import com.example.dhap.dto.application.ApplicationResponse;
import com.example.dhap.dto.application.CreateApplicationRequest;
import com.example.dhap.entities.CoordinatorApplication;
import com.example.dhap.entities.User;
import com.example.dhap.enums.ApplicationStatus;
import com.example.dhap.repositories.ApplicationRepository;
import com.example.dhap.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository        userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository        = userRepository;
    }

    // ── GET /applications?page=&size=[&status=] ───────────────────────────

    /**
     * No status filter  → all applications (coordinator overview).
     * With status filter → e.g. ?status=PENDING to review pending only.
     */
    public Page<ApplicationResponse> getAll(Pageable pageable, ApplicationStatus status) {
        if (status != null) {
            return applicationRepository
                    .findByStatus(status.name(), pageable)
                    .map(ApplicationResponse::from);
        }
        return applicationRepository.findAll(pageable).map(ApplicationResponse::from);
    }

    // ── POST /applications ────────────────────────────────────────────────

    /**
     * User applies to become a coordinator.
     *
     * One @Transactional block:
     *   1. Resolve applicant from JWT
     *   2. Create and save the application document
     *   3. Set user.isSubmitted = true (profile reflects pending state)
     */
    @Transactional
    public ApplicationResponse create(CreateApplicationRequest req) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User applicant = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        if (Boolean.TRUE.equals(applicant.getIsSubmitted())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Application already submitted");
        }
        if ("COORDINATOR".equals(applicant.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "User is already a coordinator");
        }

        CoordinatorApplication app = new CoordinatorApplication();
        app.setUserId(applicant.getId());
        app.setUserEmail(applicant.getEmail());
        app.setMessage(req.message);
        app.setSubmittedAt(LocalDateTime.now());
        app = applicationRepository.save(app);

        applicant.setIsSubmitted(true);
        userRepository.save(applicant);

        return ApplicationResponse.from(app);
    }

    // ── PATCH /applications/{id}/accept ───────────────────────────────────

    /**
     * Coordinator approves the application and promotes the user in one transaction.
     *
     * One @Transactional block:
     *   1. Set application.status = ACCEPTED
     *   2. Set user.role = "COORDINATOR"
     *      (UserResponse.isCoordinator is derived from role — no separate field needed)
     */
    @Transactional
    public ApplicationResponse accept(String id) {
        CoordinatorApplication app = findOrThrow(id);
        app.setStatus(ApplicationStatus.ACCEPTED.name());
        applicationRepository.save(app);

        User user = userRepository.findById(app.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant user not found"));
        user.setRole("COORDINATOR");
        userRepository.save(user);

        return ApplicationResponse.from(app);
    }

    // ── PATCH /applications/{id}/reject ──────────────────────────────────

    /**
     * Rejects the application and clears user.isSubmitted in one transaction
     * so the user can apply again later.
     */
    @Transactional
    public ApplicationResponse reject(String id) {
        CoordinatorApplication app = findOrThrow(id);
        app.setStatus(ApplicationStatus.REJECTED.name());
        applicationRepository.save(app);

        userRepository.findById(app.getUserId()).ifPresent(user -> {
            user.setIsSubmitted(false);
            userRepository.save(user);
        });

        return ApplicationResponse.from(app);
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private CoordinatorApplication findOrThrow(String id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Application not found"));
    }
}