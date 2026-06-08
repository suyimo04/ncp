package com.trace.util;

import cn.hutool.core.util.RandomUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 溯源码和批次号生成工具
 */
public class TraceCodeUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 生成批次号
     * 格式：BN + 日期 + 3位随机数 如 BN20240501123
     */
    public static String generateBatchNo() {
        String datePart = LocalDate.now().format(DATE_FMT);
        String randomPart = RandomUtil.randomNumbers(3);
        return "BN" + datePart + randomPart;
    }

    /**
     * 生成溯源码
     * 格式：TC + 日期 + 6位随机字母数字
     */
    public static String generateTraceCode() {
        String datePart = LocalDate.now().format(DATE_FMT);
        String randomPart = RandomUtil.randomString(6).toUpperCase();
        return "TC" + datePart + randomPart;
    }
}
