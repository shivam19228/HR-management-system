package com.hcl.hrgenius.controller;

import com.hcl.hrgenius.dto.training.TrainingRequest;
import com.hcl.hrgenius.dto.training.TrainingResponse;
import com.hcl.hrgenius.entity.Employee;
import com.hcl.hrgenius.exception.ForbiddenException;
import com.hcl.hrgenius.security.SecurityHelper;
import com.hcl.hrgenius.service.TrainingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training")
public class TrainingController {

    private final TrainingService trainingService;
    private final SecurityHelper securityHelper;

    public TrainingController(TrainingService trainingService, SecurityHelper securityHelper) {
        this.trainingService = trainingService;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    public ResponseEntity<List<TrainingResponse>> getTrainings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            HttpServletRequest request) {

        List<TrainingResponse> trainings = trainingService.getTrainings(search, status);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            Employee emp = empOpt.get();
            List<TrainingResponse> ownTrainings = trainings.stream()
                    .filter(t -> (emp.getName() != null && emp.getName().equalsIgnoreCase(t.getEmployee())))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownTrainings);
        }

        return ResponseEntity.ok(trainings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingResponse> getTrainingById(@PathVariable Long id, HttpServletRequest request) {
        TrainingResponse training = trainingService.getTrainingById(id);

        if (securityHelper.isEmployeeOnly(request)) {
            Optional<Employee> empOpt = securityHelper.getEmployeeForRequest(request);
            if (empOpt.isEmpty() || !empOpt.get().getName().equalsIgnoreCase(training.getEmployee())) {
                throw new ForbiddenException("Access denied: You can only view your own assigned training.");
            }
        }

        return ResponseEntity.ok(training);
    }

    @PostMapping
    public ResponseEntity<TrainingResponse> createTraining(@Valid @RequestBody TrainingRequest trainingRequest, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        TrainingResponse created = trainingService.createTraining(trainingRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingResponse> updateTraining(
            @PathVariable Long id,
            @Valid @RequestBody TrainingRequest trainingRequest,
            HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        TrainingResponse updated = trainingService.updateTraining(id, trainingRequest);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id, HttpServletRequest request) {
        securityHelper.requireAdminOrManager(request);
        trainingService.deleteTraining(id);
        return ResponseEntity.noContent().build();
    }
}
