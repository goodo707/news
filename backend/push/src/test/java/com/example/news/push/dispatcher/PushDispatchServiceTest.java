package com.example.news.push.dispatcher;

import com.example.news.core.domain.Article;
import com.example.news.core.domain.User;
import com.example.news.push.filter.UserFilterService;
import com.example.news.push.log.PushLogRecorder;
import com.example.news.push.notification.PushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PushDispatchServiceTest {

    @Mock UserFilterService userFilterService;
    @Mock PushNotificationService pushNotificationService;
    @Mock PushLogRecorder pushLogRecorder;

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-05-25T12:00:00Z"),
        ZoneId.of("Asia/Seoul")
    );
    private PushDispatchService pushDispatchService;

    @BeforeEach
    void setUp() {
        pushDispatchService = new PushDispatchService(
            userFilterService, pushNotificationService, pushLogRecorder, clock
        );
    }

    private Article article() {
        return new Article(
            "AKR-1", "title", "https://www.yna.co.kr/view/AKR-1",
            "author", 1L, "2026-05-25T12:00:00", "2026-05-25T12:00:00"
        );
    }

    private User userWithPushType(long id, String pushType) {
        return new User(id, "name-" + id, "device-" + id, pushType, null, null);
    }

    @Test
    void APNS_사용자는_sendAPNS_호출() {
        Article a = article();
        User u = userWithPushType(1L, "APNS");
        given(userFilterService.findTargetsForArticle(a)).willReturn(List.of(u));
        given(pushNotificationService.sendAPNS("device-1", "AKR-1", "title"))
            .willReturn("success");

        pushDispatchService.dispatch(a, "정치");

        verify(pushNotificationService).sendAPNS("device-1", "AKR-1", "title");
        verify(pushNotificationService, never()).sendFCM(anyString(), anyString(), anyString());
        verify(pushLogRecorder).record(eq(u), eq(a), eq("정치"), eq("success"), anyString());
    }

    @Test
    void FCM_사용자는_sendFCM_호출() {
        Article a = article();
        User u = userWithPushType(2L, "FCM");
        given(userFilterService.findTargetsForArticle(a)).willReturn(List.of(u));
        given(pushNotificationService.sendFCM("device-2", "AKR-1", "title"))
            .willReturn("fail");

        pushDispatchService.dispatch(a, "북한");

        verify(pushNotificationService).sendFCM("device-2", "AKR-1", "title");
        verify(pushNotificationService, never()).sendAPNS(anyString(), anyString(), anyString());
        verify(pushLogRecorder).record(eq(u), eq(a), eq("북한"), eq("fail"), anyString());
    }

    @Test
    void 알수없는_pushType_은_notification_서비스_호출_없이_fail_기록() {
        Article a = article();
        User u = userWithPushType(3L, "SMS"); // 알 수 없는 타입
        given(userFilterService.findTargetsForArticle(a)).willReturn(List.of(u));

        pushDispatchService.dispatch(a, "경제");

        verify(pushNotificationService, never()).sendAPNS(anyString(), anyString(), anyString());
        verify(pushNotificationService, never()).sendFCM(anyString(), anyString(), anyString());
        verify(pushLogRecorder).record(eq(u), eq(a), eq("경제"), eq("fail"), anyString());
    }

    @Test
    void 대상자가_없으면_notification_과_record_모두_호출_안_됨() {
        Article a = article();
        given(userFilterService.findTargetsForArticle(a)).willReturn(List.of());

        pushDispatchService.dispatch(a, "사회");

        verify(pushNotificationService, never()).sendAPNS(anyString(), anyString(), anyString());
        verify(pushNotificationService, never()).sendFCM(anyString(), anyString(), anyString());
        verify(pushLogRecorder, never())
            .record(any(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void 여러_사용자_각각_pushType_에_맞게_분기_호출() {
        Article a = article();
        User u1 = userWithPushType(1L, "APNS");
        User u2 = userWithPushType(2L, "FCM");
        given(userFilterService.findTargetsForArticle(a)).willReturn(List.of(u1, u2));
        given(pushNotificationService.sendAPNS(anyString(), anyString(), anyString()))
            .willReturn("success");
        given(pushNotificationService.sendFCM(anyString(), anyString(), anyString()))
            .willReturn("success");

        pushDispatchService.dispatch(a, "산업");

        verify(pushNotificationService).sendAPNS("device-1", "AKR-1", "title");
        verify(pushNotificationService).sendFCM("device-2", "AKR-1", "title");
        verify(pushLogRecorder).record(eq(u1), eq(a), eq("산업"), eq("success"), anyString());
        verify(pushLogRecorder).record(eq(u2), eq(a), eq("산업"), eq("success"), anyString());
    }
}
