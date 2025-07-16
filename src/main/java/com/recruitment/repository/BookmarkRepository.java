package com.recruitment.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.recruitment.entity.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser_UserId(String userId);
    boolean existsByUser_UserIdAndJob_JobId(String userId, String jobId);
    void deleteByUser_UserIdAndJob_JobId(String userId, String jobId);
}
