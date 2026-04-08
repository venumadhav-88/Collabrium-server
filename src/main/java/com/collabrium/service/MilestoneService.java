package com.collabrium.service;

import com.collabrium.dto.MilestoneDto;
import com.collabrium.exception.ApiException;
import com.collabrium.model.Milestone;
import com.collabrium.model.MilestoneStatus;
import com.collabrium.model.Project;
import com.collabrium.repository.MilestoneRepository;
import com.collabrium.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final ProjectRepository projectRepository;

    public MilestoneService(MilestoneRepository milestoneRepository, ProjectRepository projectRepository) {
        this.milestoneRepository = milestoneRepository;
        this.projectRepository = projectRepository;
    }

    public List<MilestoneDto> getAllMilestones() {
        return milestoneRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<MilestoneDto> getMilestonesByProject(Long projectId) {
        assertProjectExists(projectId);
        return milestoneRepository.findByProjectIdOrderByDueDateAsc(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @SuppressWarnings("null")
    public MilestoneDto createMilestone(Long projectId, MilestoneDto dto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ApiException.notFound("Project not found with id: " + projectId));

        Milestone milestone = Milestone.builder()
                .project(project)
                .title(dto.getTitle())
                .dueDate(dto.getDueDate())
                .status(dto.getStatus() != null ? dto.getStatus() : MilestoneStatus.PENDING)
                .progressPercentage(dto.getProgressPercentage() != null ? dto.getProgressPercentage() : 0)
                .build();

        return toDto(milestoneRepository.save(milestone));
    }

    @Transactional
    @SuppressWarnings("null")
    public MilestoneDto updateMilestone(Long id, MilestoneDto dto) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Milestone not found with id: " + id));

        if (dto.getTitle() != null)
            milestone.setTitle(dto.getTitle());
        if (dto.getDueDate() != null)
            milestone.setDueDate(dto.getDueDate());
        if (dto.getStatus() != null)
            milestone.setStatus(dto.getStatus());
        if (dto.getProgressPercentage() != null)
            milestone.setProgressPercentage(dto.getProgressPercentage());

        return toDto(milestoneRepository.save(milestone));
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Milestone not found with id: " + id));
        milestoneRepository.delete(milestone);
    }

    @SuppressWarnings("null")
    private void assertProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ApiException.notFound("Project not found with id: " + projectId);
        }
    }

    public MilestoneDto toDto(Milestone m) {
        MilestoneDto dto = MilestoneDto.builder()
                .id(m.getId())
                .projectId(m.getProject().getId())
                .title(m.getTitle())
                .dueDate(m.getDueDate())
                .status(m.getStatus())
                .progressPercentage(m.getProgressPercentage())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
        dto.setProjectTitle(m.getProject().getTitle());
        return dto;
    }
}
