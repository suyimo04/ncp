package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.BatchDTO;
import com.trace.dto.BatchStatusDTO;
import com.trace.service.BatchService;
import com.trace.common.OperationLog;
import com.trace.util.ExcelExportUtil;
import com.trace.util.QRCodeUtil;
import com.trace.vo.BatchVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * 批次管理接口
 */
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    @Autowired
    private BatchService batchService;

    /** 批次列表 */
    @GetMapping("/list")
    public Result<PageResult<BatchVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long foodId) {
        return Result.success(batchService.listBatches(pageNum, pageSize, keyword, status, foodId));
    }

    /** 批次详情 */
    @GetMapping("/{id}")
    public Result<BatchVO> detail(@PathVariable Long id) {
        return Result.success(batchService.getById(id));
    }

    /** 新增批次 */
    @OperationLog("新增生产批次")
    @PostMapping
    public Result<Void> add(@RequestBody BatchDTO dto) {
        batchService.addBatch(dto);
        return Result.success();
    }

    /** 生成溯源码 */
    @OperationLog("生成溯源码")
    @PostMapping("/trace-code/{id}")
    public Result<String> generateTraceCode(@PathVariable Long id) {
        return Result.success(batchService.generateTraceCode(id));
    }

    /** 获取批次二维码（Base64图片） */
    @GetMapping("/qrcode/{id}")
    public Result<String> qrcode(@PathVariable Long id) {
        BatchVO batch = batchService.getById(id);
        if (batch == null || batch.getTraceCode() == null) {
            return Result.error(400, "批次不存在或尚未生成溯源码");
        }
        // 二维码内容为溯源查询页面URL + 溯源码
        String qrContent = "http://localhost:5173/trace?code=" + batch.getTraceCode();
        String base64 = QRCodeUtil.generateBase64(qrContent, 300, 300);
        return Result.success(base64);
    }

    /** 更新批次状态 */
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid BatchStatusDTO dto) {
        batchService.updateStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }

    /** 导出批次列表 */
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status) throws IOException {
        PageResult<BatchVO> pageResult = batchService.listBatches(1, 10000, keyword, status, null);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (BatchVO vo : pageResult.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("batchNo", vo.getBatchNo());
            row.put("foodName", vo.getFoodName());
            row.put("producerName", vo.getProducerName());
            row.put("productionDate", vo.getProductionDate() != null ? vo.getProductionDate().toString() : "");
            row.put("expiryDate", vo.getExpiryDate() != null ? vo.getExpiryDate().toString() : "");
            row.put("quantity", vo.getQuantity());
            row.put("traceCode", vo.getTraceCode());
            row.put("status", vo.getStatus());
            dataList.add(row);
        }
        List<String> headers = Arrays.asList("批次号", "食品名称", "生产企业", "生产日期", "过期日期", "数量", "溯源码", "状态");
        List<String> keys = Arrays.asList("batchNo", "foodName", "producerName", "productionDate", "expiryDate", "quantity", "traceCode", "status");
        ExcelExportUtil.export(response, "批次列表", headers, keys, dataList);
    }
}
