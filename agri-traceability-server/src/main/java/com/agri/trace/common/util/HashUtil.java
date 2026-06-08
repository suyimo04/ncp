package com.agri.trace.common.util;

import com.agri.trace.entity.BizCertificate;
import com.agri.trace.entity.BizProductBatch;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HashUtil {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private HashUtil() {
    }

    public static String sha256(String content) {
        return digest(content.getBytes(StandardCharsets.UTF_8));
    }

    public static String sha256File(MultipartFile file) throws IOException {
        return digest(file.getBytes());
    }

    public static String sha256File(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            return toHex(md.digest());
        } catch (Exception e) {
            throw new IOException("文件哈希计算失败", e);
        }
    }

    public static String batchHash(BizProductBatch batch) {
        // 批次哈希只选取答辩中需要验证的稳定字段，避免备注等展示字段影响存证结果。
        String raw = join(
                batch.getBatchCode(),
                batch.getProducerCode(),
                batch.getProductName(),
                batch.getProductCategory(),
                batch.getOriginAddress(),
                formatDate(batch.getHarvestTime()),
                formatDecimal(batch.getBatchWeight())
        );
        return sha256(raw);
    }

    public static String certificateHash(BizCertificate cert, BizProductBatch batch) {
        String raw = join(
                cert.getCertificateCode(),
                cert.getBatchCode(),
                cert.getReportCode(),
                batch == null ? "" : batch.getProducerCode(),
                formatDateTime(cert.getIssueTime()),
                formatDate(cert.getExpireTime()),
                cert.getAuditUserId() == null ? "" : String.valueOf(cert.getAuditUserId())
        );
        return sha256(raw);
    }

    public static String revokeHash(String certificateCode, String revokeReason, LocalDateTime revokeTime, Long operatorId) {
        return sha256(join(certificateCode, revokeReason, formatDateTime(revokeTime), operatorId == null ? "" : String.valueOf(operatorId)));
    }

    private static String digest(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    private static String join(String... values) {
        return String.join("|", values);
    }

    private static String formatDate(LocalDate value) {
        return value == null ? "" : DATE.format(value);
    }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }

    private static String formatDecimal(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}
