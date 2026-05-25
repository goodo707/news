package com.example.news.core.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_category")
@Getter
@NoArgsConstructor
public class UserCategory {

    @EmbeddedId
    private UserCategoryId id;

    public UserCategory(UserCategoryId id) {
        this.id = id;
    }
}
