package com.example.news.core.repository;

import com.example.news.core.domain.PushHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushHistoryRepository extends JpaRepository<PushHistory, Long> {
}
