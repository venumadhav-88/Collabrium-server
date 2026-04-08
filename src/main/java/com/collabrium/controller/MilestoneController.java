package com.collabrium.controller;

import com.collabrium.dto.ApiResponse;
import com.collabrium.dto.MilestoneDto;
import com.collabrium.service.MilestoneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @GetMapping("/api/milestones")
    public ResponseEntity<ApiResponse<List<MilestoneDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.getAllMilestones()));
    }

    @GetMapping("/api/projects/{projectId}/milestones")
    public ResponseEntity<ApiResponse<List<MilestoneDto>>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.getMilestonesByProject(projectId)));
    }

    @PostMapping("/api/projects/{projectId}/milestones")
    public ResponseEntity<ApiResponse<MilestoneDto>> create(@PathVariable Long projectId,
                                               @Valid @RequestBody MilestoneDto dto) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.createMilestone(projectId, dto)));
    }

    @PutMapping("/api/milestones/{id}")
    public ResponseEntity<ApiResponse<MilestoneDto>> update(@PathVariable Long id,
                                               @RequestBody MilestoneDto dto) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.updateMilestone(id, dto)));
    }

    @DeleteMapping("/api/milestones/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        milestoneService.deleteMilestone(id);
        return ResponseEntity.noContent().build();
    }
}
