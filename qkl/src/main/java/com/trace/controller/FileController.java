package com.trace.controller;

import cn.hutool.core.io.FileUtil;
import com.trace.common.Result;
import com.trace.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件上传接口
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Value("${file.upload-path}")
    private String uploadPath;

    /** 上传文件 */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return Result.success(url);
    }

    /** 文件预览/下载 */
    @GetMapping("/preview/**")
    public void preview(HttpServletResponse response, jakarta.servlet.http.HttpServletRequest request) throws IOException {
        // 获取请求路径中 /api/file/preview/ 后面的部分
        String path = request.getRequestURI().replace("/api/file/preview/", "");
        File file = new File(uploadPath, path);
        if (!file.exists()) {
            response.setStatus(404);
            response.getWriter().write("文件不存在");
            return;
        }
        // 设置响应头
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=" +
                URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));
        Files.copy(file.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }
}
