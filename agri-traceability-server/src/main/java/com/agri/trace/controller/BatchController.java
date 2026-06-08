package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.BatchDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.entity.BizProductBatch;
import com.agri.trace.service.BatchService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/batch")
public class BatchController {
    private final BatchService batchService;

    @GetMapping("/page")
    public R<IPage<BizProductBatch>> page(PageQueryDTO query) {
        return R.ok(batchService.page(query));
    }

    @GetMapping("/{id}")
    public R<BizProductBatch> detail(@PathVariable Long id) {
        return R.ok(batchService.detail(id));
    }

    @PostMapping
    public R<BizProductBatch> create(@Valid @RequestBody BatchDTO dto) {
        return R.ok(batchService.create(dto));
    }

    @PutMapping("/{id}")
    public R<BizProductBatch> update(@PathVariable Long id, @Valid @RequestBody BatchDTO dto) {
        return R.ok(batchService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        batchService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/hash")
    public R<BizProductBatch> hash(@PathVariable Long id) {
        return R.ok(batchService.generateHash(id));
    }

    @PostMapping("/{id}/chain")
    public R<BizProductBatch> chain(@PathVariable Long id) {
        return R.ok(batchService.chain(id));
    }
}
