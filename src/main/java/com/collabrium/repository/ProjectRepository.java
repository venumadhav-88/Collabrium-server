package com.collabrium.repository;

import com.collabrium.model.Project;
import com.collabrium.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCreatedBy(User createdBy);

    List<Project> findByCreatedByOrderByCreatedAtDesc(User createdBy);
}
