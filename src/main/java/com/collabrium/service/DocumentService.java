package com.collabrium.service;

import com.collabrium.dto.DocumentDto;
import com.collabrium.dto.UserDto;
import com.collabrium.exception.ApiException;
import com.collabrium.model.Document;
import com.collabrium.model.Project;
import com.collabrium.model.User;
import com.collabrium.repository.DocumentRepository;
import com.collabrium.repository.ProjectRepository;
import com.collabrium.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public DocumentService(DocumentRepository documentRepository, ProjectRepository projectRepository,
            UserRepository userRepository, ModelMapper modelMapper) {
        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DocumentDto> getDocumentsByProject(Long projectId) {
        assertProjectExists(projectId);
        return documentRepository.findByProjectIdOrderByUploadedAtDesc(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @SuppressWarnings("null")
    public DocumentDto uploadDocument(Long projectId, DocumentDto dto, String uploaderEmail) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> ApiException.notFound("Project not found with id: " + projectId));

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> ApiException.notFound("User not found: " + uploaderEmail));

        Document document = Document.builder()
                .project(project)
                .uploadedBy(uploader)
                .fileName(dto.getFileName())
                .fileUrl(dto.getFileUrl() != null ? dto.getFileUrl() : "")
                .version(dto.getVersion() != null ? dto.getVersion() : "1.0.0")
                .build();

        document.setTag(dto.getTag() != null ? dto.getTag() : "Other");
        document.setFileSize(dto.getFileSize() != null ? dto.getFileSize() : "—");

        return toDto(documentRepository.save(document));
    }

    @Transactional
    @SuppressWarnings("null")
    public void deleteDocument(Long projectId, Long documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> ApiException.notFound("Document not found: " + documentId));
        if (!doc.getProject().getId().equals(projectId)) {
            throw ApiException.forbidden("Document does not belong to this project");
        }
        documentRepository.delete(doc);
    }

    @SuppressWarnings("null")
    private void assertProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ApiException.notFound("Project not found with id: " + projectId);
        }
    }

    private DocumentDto toDto(Document d) {
        UserDto uploadedByDto = modelMapper.map(d.getUploadedBy(), UserDto.class);
        DocumentDto dto = DocumentDto.builder()
                .id(d.getId())
                .projectId(d.getProject().getId())
                .fileName(d.getFileName())
                .fileUrl(d.getFileUrl())
                .version(d.getVersion())
                .uploadedBy(uploadedByDto)
                .uploadedAt(d.getUploadedAt())
                .build();
        dto.setTag(d.getTag());
        dto.setFileSize(d.getFileSize());
        dto.setProjectTitle(d.getProject().getTitle());
        return dto;
    }
}
