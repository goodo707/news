package com.example.news.push.filter;

import com.example.news.core.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

/**
 * 사용자의 방해 금지 시간대(DND) 여부를 판정한다.
 *
 * <p>경계값 정책: <b>양쪽 포함 [start, end]</b> — 시작/종료 시각 모두 차단한다.
 * <pre>
 *   start &lt;= end  : 일반 구간    [start, end]
 *   start &gt;  end  : 자정 넘김    [start, 24:00) ∪ [00:00, end]
 *   둘 다 null      : 미설정 (Excel 의 "-") → 항상 false (발송 허용)
 * </pre>
 */
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
