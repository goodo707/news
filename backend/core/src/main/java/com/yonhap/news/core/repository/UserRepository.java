package com.yonhap.news.core.repository;

import com.yonhap.news.core.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
