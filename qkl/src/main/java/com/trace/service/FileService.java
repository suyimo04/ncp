package com.trace.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.trace.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件上传服务
 */
@Slf4j
@Service
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 上传文件
     * @return 文件访问路径
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dir = uploadPath + datePath;
        FileUtil.mkdir(dir);

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String fileName = IdUtil.simpleUUID() + ext;

        File destFile = new File(dir, fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }

        // 返回相对路径
        String relativePath = "/uploads/" + datePath + "/" + fileName;
        log.info("文件上传成功: {}", relativePath);
        return relativePath;
    }
}
