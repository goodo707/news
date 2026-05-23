package com.example.news.core.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "push_log")
@Getter
@NoArgsConstructor
public class PushHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "push_type", nullable = false)
    private String pushType;

    @Column(name = "article_id", nullable = false)
    private String articleId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(name = "sent_at", nullable = false)
    private String sentAt;

    @Column(nullable = false)
    private String status;

    public PushHistory(String deviceId, String pushType, String articleId,
                       String title, String category, String sentAt, String status) {
        this.deviceId = deviceId;
        this.pushType = pushType;
        this.articleId = articleId;
        this.title = title;
        this.category = category;
        this.sentAt = sentAt;
        this.status = status;
    }
}
