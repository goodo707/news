package com.example.news.push.history;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.PushHistory;
import com.example.news.core.domain.User;
import com.example.news.core.repository.PushHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushHistoryRecorder {

    private final PushHistoryRepository pushHistoryRepository;

    public void record(User user, Article article, String categoryName,
                       String status, String sentAt) {
        pushHistoryRepository.save(new PushHistory(
            user.getDeviceId(),
            user.getPushType(),
            article.getArticleId(),
            article.getTitle(),
            categoryName,
            sentAt,
            status
        ));
    }
}
