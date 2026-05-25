package com.example.news.push.log;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.PushLog;
import com.example.news.core.domain.User;
import com.example.news.core.repository.PushLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushLogRecorder {

    private final PushLogRepository pushLogRepository;

    public void record(User user, Article article, String categoryName,
                       String status, String sentAt) {
        pushLogRepository.save(new PushLog(
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
