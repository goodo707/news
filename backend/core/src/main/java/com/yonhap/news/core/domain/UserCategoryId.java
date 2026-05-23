package com.yonhap.news.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "category_id")
    private Long categoryId;
}
