package com.jobportal.repository;

import com.jobportal.model.Application;
import com.jobportal.model.Job;
import com.jobportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidate(User candidate);
    List<Application> findByJob(Job job);
    Optional<Application> findByJobAndCandidate(Job job, User candidate);
    List<Application> findByJobOrderByMatchScoreDesc(Job job);
    boolean existsByResume(com.jobportal.model.Resume resume);
}
