package com.hcl.hrgenius.service;

import com.hcl.hrgenius.dto.training.TrainingRequest;
import com.hcl.hrgenius.dto.training.TrainingResponse;
import com.hcl.hrgenius.entity.TrainingProgram;
import com.hcl.hrgenius.exception.ResourceNotFoundException;
import com.hcl.hrgenius.repository.TrainingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Transactional(readOnly = true)
    public List<TrainingResponse> getTrainings(String search, String status) {
        List<TrainingProgram> list;
        if ((search == null || search.trim().isEmpty()) && (status == null || status.trim().isEmpty() || "All".equalsIgnoreCase(status))) {
            list = trainingRepository.findAll();
        } else {
            list = trainingRepository.searchTrainings(search, status);
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TrainingResponse getTrainingById(Long id) {
        TrainingProgram tr = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training program not found with id: " + id));
        return mapToResponse(tr);
    }

    public TrainingResponse createTraining(TrainingRequest request) {
        TrainingProgram tr = new TrainingProgram();
        tr.setEmployee(request.getEmployee());
        tr.setTrainingName(request.getTrainingName());
        tr.setTrainer(request.getTrainer());
        tr.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        tr.setEndDate(request.getEndDate() != null ? request.getEndDate() : LocalDate.now().plusDays(14));
        tr.setStatus(request.getStatus() != null ? request.getStatus() : "Upcoming");

        TrainingProgram saved = trainingRepository.save(tr);
        return mapToResponse(saved);
    }

    public TrainingResponse updateTraining(Long id, TrainingRequest request) {
        TrainingProgram tr = trainingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training program not found with id: " + id));

        if (request.getEmployee() != null) tr.setEmployee(request.getEmployee());
        if (request.getTrainingName() != null) tr.setTrainingName(request.getTrainingName());
        if (request.getTrainer() != null) tr.setTrainer(request.getTrainer());
        if (request.getStartDate() != null) tr.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) tr.setEndDate(request.getEndDate());
        if (request.getStatus() != null) tr.setStatus(request.getStatus());

        TrainingProgram updated = trainingRepository.save(tr);
        return mapToResponse(updated);
    }

    public void deleteTraining(Long id) {
        if (!trainingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Training program not found with id: " + id);
        }
        trainingRepository.deleteById(id);
    }

    private TrainingResponse mapToResponse(TrainingProgram tr) {
        TrainingResponse resp = new TrainingResponse();
        resp.setId(tr.getId());
        resp.setEmployee(tr.getEmployee());
        resp.setTrainingName(tr.getTrainingName());
        resp.setTrainer(tr.getTrainer());
        resp.setStartDate(tr.getStartDate());
        resp.setEndDate(tr.getEndDate());
        resp.setStatus(tr.getStatus());
        return resp;
    }
}
