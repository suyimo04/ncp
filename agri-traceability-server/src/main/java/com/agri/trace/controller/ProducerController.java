package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.AuditDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.dto.ProducerDTO;
import com.agri.trace.entity.BizProducer;
import com.agri.trace.service.ProducerService;
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
@RequestMapping("/api/producer")
public class ProducerController {
    private final ProducerService producerService;

    @GetMapping("/page")
    public R<IPage<BizProducer>> page(PageQueryDTO query) {
        return R.ok(producerService.page(query));
    }

    @GetMapping("/{id}")
    public R<BizProducer> detail(@PathVariable Long id) {
        return R.ok(producerService.detail(id));
    }

    @PostMapping
    public R<BizProducer> create(@Valid @RequestBody ProducerDTO dto) {
        return R.ok(producerService.create(dto));
    }

    @PutMapping("/{id}")
    public R<BizProducer> update(@PathVariable Long id, @Valid @RequestBody ProducerDTO dto) {
        return R.ok(producerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        producerService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/audit")
    public R<BizProducer> audit(@PathVariable Long id, @Valid @RequestBody AuditDTO dto) {
        return R.ok(producerService.audit(id, dto));
    }

    @GetMapping("/current")
    public R<Long> currentProducerId() {
        return R.ok(producerService.currentProducerId());
    }
}
