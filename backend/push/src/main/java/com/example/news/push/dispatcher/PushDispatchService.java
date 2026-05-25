package com.example.news.push.dispatcher;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.User;
import com.example.news.push.filter.UserFilterService;
import com.example.news.push.history.PushHistoryRecorder;
import com.example.news.push.notification.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushDispatchService {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final UserFilterService userFilterService;
    private final PushNotificationService pushNotificationService;
    private final PushHistoryRecorder pushHistoryRecorder;
    private final Clock clock;

    @Transactional
    public void dispatch(Article article, String categoryName) {
        List<User> targets = userFilterService.findTargetsForArticle(article);
        if (targets.isEmpty()) return;

        String sentAt = LocalDateTime.now(clock).format(FORMATTER);
        int success = 0, fail = 0;

        for (User user : targets) {
            String status = send(user, article);
            pushHistoryRecorder.record(user, article, categoryName, status, sentAt);
            if ("success".equals(status)) success++; else fail++;
        }

        log.info("[push] {} ({}): 대상 {}명, 성공 {}건, 실패 {}건",
            article.getArticleId(), categoryName, targets.size(), success, fail);
    }

    private String send(User user, Article article) {
        return switch (user.getPushType()) {
            case "APNS" -> pushNotificationService.sendAPNS(
                user.getDeviceId(), article.getArticleId(), article.getTitle());
            case "FCM"  -> pushNotificationService.sendFCM(
                user.getDeviceId(), article.getArticleId(), article.getTitle());
            default -> "fail";
        };
    }
}
