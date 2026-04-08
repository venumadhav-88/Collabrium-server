package com.collabrium.service;

import com.collabrium.dto.MilestoneDto;
import com.collabrium.dto.ProjectDto;
import com.collabrium.dto.UserDto;
import com.collabrium.exception.ApiException;
import com.collabrium.model.Project;
import com.collabrium.model.ProjectStatus;
import com.collabrium.model.User;
import com.collabrium.repository.ProjectRepository;
import com.collabrium.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final MilestoneService milestoneService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository,
            ModelMapper modelMapper, MilestoneService milestoneService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.milestoneService = milestoneService;
    }

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<ProjectDto> getProjectsByUser(String email) {
        User user = getUserByEmail(email);
        return projectRepository.findByCreatedByOrderByCreatedAtDesc(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProjectDto getProjectById(Long id) {
        Project project = findProjectById(id);
        return toDto(project);
    }

    @Transactional
    @SuppressWarnings("null")
    public ProjectDto createProject(ProjectDto dto, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);

        Project project = Project.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : ProjectStatus.ACTIVE)
                .createdBy(creator)
                .build();

        return toDto(projectRepository.save(project));
    }

    @Transactional
    @SuppressWarnings("null")
    public ProjectDto updateProject(Long id, ProjectDto dto, String requesterEmail) {
        Project project = findProjectById(id);
        assertOwnerOrAdmin(project, requesterEmail);

        if (dto.getTitle() != null)
            project.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            project.setDescription(dto.getDescription());
        if (dto.getStatus() != null)
            project.setStatus(dto.getStatus());

        return toDto(projectRepository.save(project));
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteProject(Long id, String requesterEmail) {
        Project project = findProjectById(id);
        assertOwnerOrAdmin(project, requesterEmail);
        projectRepository.delete(project);
    }

    // ──── Helpers ────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    private Project findProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Project not found with id: " + id));
    }

    @SuppressWarnings("null")
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found with email: " + email));
    }

    private void assertOwnerOrAdmin(Project project, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        boolean isOwner = project.getCreatedBy().getEmail().equals(requesterEmail);
        boolean isAdmin = requester.getRole().name().equals("ADMIN");
        if (!isOwner && !isAdmin) {
            throw ApiException.forbidden("You do not have permission to modify this project");
        }
    }

    public ProjectDto toDto(Project project) {
        UserDto createdByDto = modelMapper.map(project.getCreatedBy(), UserDto.class);
        List<MilestoneDto> milestones = project.getMilestones() != null
                ? project.getMilestones().stream().map(milestoneService::toDto).collect(Collectors.toList())
                : List.of();

        ProjectDto dto = ProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .createdBy(createdByDto)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
        dto.setMilestones(milestones);
        return dto;
    }
}
