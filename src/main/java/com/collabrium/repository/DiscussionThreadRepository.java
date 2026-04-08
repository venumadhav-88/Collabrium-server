package com.collabrium.repository;

import com.collabrium.model.DiscussionThread;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiscussionThreadRepository extends JpaRepository<DiscussionThread, Long> {
    List<DiscussionThread> findAllByOrderByUpdatedAtDesc();
    List<DiscussionThread> findByProjectIdOrderByUpdatedAtDesc(Long projectId);
}
