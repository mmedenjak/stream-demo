package com.ingemark.stream.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeMarkLogger {
    private final Logger log;
    private final String tag;
    private volatile Long timeMarker;

    public TimeMarkLogger(String tag) {
        this(tag, TimeMarkLogger.class);
    }

    public TimeMarkLogger(String tag, Class<?> c) {
        this.tag = tag;
        this.log = LoggerFactory.getLogger(c);
    }

    public void logTimeMark() {
        final Long lastMark = timeMarker;
        final long mark = System.currentTimeMillis() / 10000;
        if (mark == (lastMark != null ? lastMark : 0)) return;
        log.info("Time mark {} {}", tag, mark);
        timeMarker = mark;
    }

}
