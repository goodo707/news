package com.example.news.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class UserCategoryId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;

    public UserCategoryId(Long userId, Long categoryId) {
        this.userId = userId;
        this.categoryId = categoryId;
    }
}
