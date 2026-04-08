package com.collabrium.controller;

import com.collabrium.dto.ApiResponse;
import com.collabrium.dto.DiscussionThreadDto;
import com.collabrium.dto.MessageDto;
import com.collabrium.service.DiscussionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
public class DiscussionController {

    private final DiscussionService discussionService;

    public DiscussionController(DiscussionService discussionService) {
        this.discussionService = discussionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscussionThreadDto>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(discussionService.getAllThreads()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiscussionThreadDto>> createThread(@RequestBody DiscussionThreadDto dto, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.createThread(dto, auth.getName())));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.getMessages(id)));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessage(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body,
                                                  Authentication auth) {
        String content = body.get("content");
        return ResponseEntity.ok(ApiResponse.success(discussionService.sendMessage(id, content, auth.getName())));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<DiscussionThreadDto>> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(discussionService.resolveThread(id)));
    }
}
