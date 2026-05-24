package com.example.news.push.filter;

import com.example.news.core.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DndChecker {

    private final Clock clock;

    public boolean isInDnd(User user) {
        if (user.getDndStart() == null || user.getDndEnd() == null) return false;
        LocalTime now = LocalTime.now(clock);
        LocalTime start = LocalTime.parse(user.getDndStart());
        LocalTime end = LocalTime.parse(user.getDndEnd());
        if (start.isBefore(end)) {
            // 일반 구간: [start, end] 양쪽 포함
            return !now.isBefore(start) && !now.isAfter(end);
        } else {
            // 자정 넘김: [start, 24:00) ∪ [00:00, end]
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }
}
