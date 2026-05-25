package com.example.news.push.log;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.PushLog;
import com.example.news.core.domain.User;
import com.example.news.core.repository.PushLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 푸시 발송 이력을 push_log 에 저장한다.
 *
 * <p>{@code title}, {@code category} 를 비정규화하여 텍스트로 직접 저장하는 이유 —
 * article 은 1000건 제한으로 삭제되지만 발송 이력은 보존되어야 하기 때문.
 * 따라서 push_log 단독으로 조회 가능하며, article 과의 JOIN 에 의존하지 않는다.
 */
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
