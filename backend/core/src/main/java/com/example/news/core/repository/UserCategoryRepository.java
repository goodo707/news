package com.example.news.core.repository;

import com.example.news.core.domain.UserCategory;
import com.example.news.core.domain.UserCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCategoryRepository extends JpaRepository<UserCategory, UserCategoryId> {
    List<UserCategory> findById_CategoryId(Long categoryId);
}
