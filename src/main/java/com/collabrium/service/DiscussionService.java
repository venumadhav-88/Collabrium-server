package com.collabrium.service;

import com.collabrium.dto.DiscussionThreadDto;
import com.collabrium.dto.MessageDto;
import com.collabrium.exception.ApiException;
import com.collabrium.model.DiscussionThread;
import com.collabrium.model.Message;
import com.collabrium.model.Project;
import com.collabrium.model.User;
import com.collabrium.repository.DiscussionThreadRepository;
import com.collabrium.repository.MessageRepository;
import com.collabrium.repository.ProjectRepository;
import com.collabrium.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionService {

    private final DiscussionThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public DiscussionService(DiscussionThreadRepository threadRepository,
                             MessageRepository messageRepository,
                             ProjectRepository projectRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<DiscussionThreadDto> getAllThreads() {
        return threadRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(t -> toDto(t, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public DiscussionThreadDto createThread(DiscussionThreadDto dto, String creatorEmail) {
        User creator = getUser(creatorEmail);
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> ApiException.notFound("Project not found: " + dto.getProjectId()));

        DiscussionThread thread = new DiscussionThread();
        thread.setTitle(dto.getTitle());
        thread.setProject(project);
        thread.setCreatedBy(creator);
        thread.setStatus("ACTIVE");

        return toDto(threadRepository.save(thread), false);
    }

    public List<MessageDto> getMessages(Long threadId) {
        assertThreadExists(threadId);
        return messageRepository.findByThreadIdOrderBySentAtAsc(threadId).stream()
                .map(this::toMsgDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendMessage(Long threadId, String content, String senderEmail) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> ApiException.notFound("Thread not found: " + threadId));
        User sender = getUser(senderEmail);

        Message msg = new Message();
        msg.setThread(thread);
        msg.setSender(sender);
        msg.setContent(content);

        Message saved = messageRepository.save(msg);

        // Update thread's updatedAt via a touch
        thread.setUpdatedAt(saved.getSentAt());
        threadRepository.save(thread);

        // Notify thread creator if different from sender
        if (!thread.getCreatedBy().getEmail().equals(senderEmail)) {
            notificationService.createNotification(
                    thread.getCreatedBy(),
                    "New message in \"" + thread.getTitle() + "\"",
                    sender.getName() + ": " + content,
                    "discussion"
            );
        }

        return toMsgDto(saved);
    }

    @Transactional
    public DiscussionThreadDto resolveThread(Long threadId) {
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> ApiException.notFound("Thread not found: " + threadId));
        thread.setStatus("RESOLVED");
        return toDto(threadRepository.save(thread), false);
    }

    private void assertThreadExists(Long threadId) {
        if (!threadRepository.existsById(threadId)) {
            throw ApiException.notFound("Thread not found: " + threadId);
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.notFound("User not found: " + email));
    }

    private DiscussionThreadDto toDto(DiscussionThread t, boolean includeMessages) {
        DiscussionThreadDto dto = new DiscussionThreadDto();
        dto.setId(t.getId());
        dto.setProjectId(t.getProject().getId());
        dto.setProjectTitle(t.getProject().getTitle());
        dto.setTitle(t.getTitle());
        dto.setStatus(t.getStatus());
        dto.setCreatedByName(t.getCreatedBy().getName());
        dto.setMessageCount(t.getMessages() != null ? t.getMessages().size() : 0);
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        if (includeMessages && t.getMessages() != null) {
            dto.setMessages(t.getMessages().stream().map(this::toMsgDto).collect(Collectors.toList()));
        }
        return dto;
    }

    private MessageDto toMsgDto(Message m) {
        MessageDto dto = new MessageDto();
        dto.setId(m.getId());
        dto.setThreadId(m.getThread().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setSenderName(m.getSender().getName());
        dto.setContent(m.getContent());
        dto.setSentAt(m.getSentAt());
        return dto;
    }
}
