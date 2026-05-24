package com.example.news.push.filter;

import com.example.news.core.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class DndCheckerTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    // 항상 time을 현재 시각으로 인식하는 DndChecker
    private DndChecker checkerAt(LocalTime time) {
        Clock fixed = Clock.fixed(
            time.atDate(LocalDate.of(2026, 1, 1)).atZone(SEOUL).toInstant(),
            SEOUL
        );
        return new DndChecker(fixed);
    }

    private User userWithDnd(String start, String end) {
        return new User(1L, "테스트", "device-1", "FCM", start, end);
    }

    @Test
    void nullDnd_always_false() {
        User user = new User(1L, "테스트", "device-1", "FCM", null, null);
        assertThat(checkerAt(LocalTime.of(12, 0)).isInDnd(user)).isFalse();
    }

    // 일반 구간: 09:00-18:00
    @Test
    void normal_beforeStart_false() {
        assertThat(checkerAt(LocalTime.of(8, 59)).isInDnd(userWithDnd("09:00", "18:00"))).isFalse();
    }

    @Test
    void normal_atStart_true() {
        assertThat(checkerAt(LocalTime.of(9, 0)).isInDnd(userWithDnd("09:00", "18:00"))).isTrue();
    }

    @Test
    void normal_atEnd_true() {
        assertThat(checkerAt(LocalTime.of(18, 0)).isInDnd(userWithDnd("09:00", "18:00"))).isTrue();
    }

    @Test
    void normal_afterEnd_false() {
        assertThat(checkerAt(LocalTime.of(18, 1)).isInDnd(userWithDnd("09:00", "18:00"))).isFalse();
    }

    // 자정 넘김 구간: 23:00-01:00
    @Test
    void midnight_beforeStart_false() {
        assertThat(checkerAt(LocalTime.of(22, 59)).isInDnd(userWithDnd("23:00", "01:00"))).isFalse();
    }

    @Test
    void midnight_atStart_true() {
        assertThat(checkerAt(LocalTime.of(23, 0)).isInDnd(userWithDnd("23:00", "01:00"))).isTrue();
    }

    @Test
    void midnight_atMidnight_true() {
        assertThat(checkerAt(LocalTime.of(0, 0)).isInDnd(userWithDnd("23:00", "01:00"))).isTrue();
    }

    @Test
    void midnight_atEnd_true() {
        assertThat(checkerAt(LocalTime.of(1, 0)).isInDnd(userWithDnd("23:00", "01:00"))).isTrue();
    }

    @Test
    void midnight_afterEnd_false() {
        assertThat(checkerAt(LocalTime.of(1, 1)).isInDnd(userWithDnd("23:00", "01:00"))).isFalse();
    }
}
