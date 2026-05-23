package com.yonhap.news.core.repository;

import com.yonhap.news.core.domain.UserCategory;
import com.yonhap.news.core.domain.UserCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory, UserCategoryId> {
}
