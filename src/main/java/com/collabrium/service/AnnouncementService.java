package com.collabrium.service;

import com.collabrium.dto.AnnouncementDto;
import com.collabrium.exception.ApiException;
import com.collabrium.model.Announcement;
import com.collabrium.model.User;
import com.collabrium.repository.AnnouncementRepository;
import com.collabrium.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AnnouncementService(AnnouncementRepository announcementRepository,
                               UserRepository userRepository,
                               NotificationService notificationService) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<AnnouncementDto> getAll() {
        return announcementRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementDto create(AnnouncementDto dto, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> ApiException.notFound("User not found: " + authorEmail));

        Announcement ann = new Announcement();
        ann.setTitle(dto.getTitle());
        ann.setContent(dto.getContent());
        ann.setAuthor(author);
        ann.setPriority(dto.getPriority() != null ? dto.getPriority() : "low");

        Announcement saved = announcementRepository.save(ann);

        // Notify all users about new announcement
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (!u.getEmail().equals(authorEmail)) {
                notificationService.createNotification(u, "New Announcement",
                        author.getName() + " posted: " + dto.getTitle(), "system");
            }
        }

        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Announcement ann = announcementRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Announcement not found: " + id));
        announcementRepository.delete(ann);
    }

    private AnnouncementDto toDto(Announcement a) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setAuthorName(a.getAuthor().getName());
        dto.setPriority(a.getPriority());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
