package com.agri.trace.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class CodeUtil {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private CodeUtil() {
    }

    public static String producerCode() {
        return "PRD" + DAY.format(LocalDateTime.now()) + random();
    }

    public static String batchCode() {
        return "BAT" + DAY.format(LocalDateTime.now()) + random();
    }

    public static String reportCode() {
        return "RPT" + DAY.format(LocalDateTime.now()) + random();
    }

    public static String certificateCode() {
        return "HGZ" + TIME.format(LocalDateTime.now()) + random();
    }

    private static String random() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999));
    }
}
