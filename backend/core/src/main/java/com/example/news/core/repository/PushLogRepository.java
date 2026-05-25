package com.example.news.core.repository;

import com.example.news.core.domain.PushLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushLogRepository extends JpaRepository<PushLog, Long> {
}
