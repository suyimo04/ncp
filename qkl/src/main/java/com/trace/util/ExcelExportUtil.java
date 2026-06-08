package com.trace.util;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Excel导出工具
 */
public class ExcelExportUtil {

    /**
     * 导出Excel到响应流
     * @param response HTTP响应
     * @param fileName 文件名（不含后缀）
     * @param headers 表头名称列表
     * @param keys 数据字段key列表（与headers一一对应）
     * @param dataList 数据列表
     */
    public static void export(HttpServletResponse response, String fileName,
                               List<String> headers, List<String> keys,
                               List<Map<String, Object>> dataList) throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        // 写表头别名
        for (int i = 0; i < headers.size(); i++) {
            writer.addHeaderAlias(keys.get(i), headers.get(i));
        }
        // 只输出有别名的字段
        writer.setOnlyAlias(true);
        writer.write(dataList, true);
        // 自动调整列宽
        for (int i = 0; i < headers.size(); i++) {
            writer.setColumnWidth(i, 20);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" +
                URLEncoder.encode(fileName + ".xlsx", StandardCharsets.UTF_8));

        writer.flush(response.getOutputStream(), true);
        writer.close();
    }
}
