package com.example.news.core.util;

import java.time.format.DateTimeFormatter;

/**
 * 시간 포맷 상수. SQLite 의 TEXT 컬럼에 저장되는 모든 시각은 동일 패턴을 사용한다.
 * (ISO 8601 local datetime, 초 단위)
 */
public final class TimeFormats {

    public static final DateTimeFormatter ISO_LOCAL_DATE_TIME =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private TimeFormats() {}
}
