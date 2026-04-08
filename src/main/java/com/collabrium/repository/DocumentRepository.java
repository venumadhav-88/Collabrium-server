package com.collabrium.repository;

import com.collabrium.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByProjectId(Long projectId);

    List<Document> findByProjectIdOrderByUploadedAtDesc(Long projectId);
}
