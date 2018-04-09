package com.thousandsunny.common;

import com.google.common.cache.Cache;

import java.time.LocalDate;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.*;

/**
 * 如果这些代码有用，那它们是guitarist在8/23/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public class CacheableUtil {
    public static LocalDate localDate = LocalDate.now();
    public static Cache<String, String> _10SecondsContainer = newBuilder().maximumSize(1000)
            .expireAfterAccess(10, SECONDS)
            .expireAfterWrite(10, SECONDS).build();
    public static Cache<String, String> _2MinutesContainer = newBuilder().maximumSize(1000)
            .expireAfterAccess(2, MINUTES)
            .expireAfterWrite(2, MINUTES).build();
    public static Cache<String, String> _5MinutesContainer = newBuilder().maximumSize(1000)
            .expireAfterAccess(5, MINUTES)
            .expireAfterWrite(5, MINUTES).build();
    public static Cache<String, String> _1DayContainer = newBuilder().maximumSize(1000)
            .expireAfterAccess(1, DAYS)
            .expireAfterWrite(1, DAYS).build();

    public static void cleanUp() {
        _10SecondsContainer.cleanUp();
        _2MinutesContainer.cleanUp();
        _5MinutesContainer.cleanUp();
        _1DayContainer.cleanUp();
    }
}
