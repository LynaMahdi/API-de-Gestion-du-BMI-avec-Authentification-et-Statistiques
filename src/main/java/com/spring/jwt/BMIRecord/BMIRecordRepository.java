package com.spring.jwt.BMIRecord;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BMIRecordRepository extends JpaRepository<BMIRecord, Integer> {
    List<BMIRecord> findByUserId(int userId);
}
