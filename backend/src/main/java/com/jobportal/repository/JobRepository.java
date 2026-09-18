package com.jobportal.repository;

import com.jobportal.model.Job;
import com.jobportal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByRecruiter(User recruiter, Pageable pageable);

    @Query("""
           select j from Job j
           where j.active = true
           and (:keyword is null or lower(j.title) like lower(concat('%', :keyword, '%'))
                or lower(j.description) like lower(concat('%', :keyword, '%')))
           and (:location is null or lower(j.location) like lower(concat('%', :location, '%')))
           and (:jobType is null or j.jobType = :jobType)
           """)
    Page<Job> search(@Param("keyword") String keyword,
                      @Param("location") String location,
                      @Param("jobType") Job.JobType jobType,
                      Pageable pageable);
}
