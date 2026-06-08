package com.agri.trace.controller;

import com.agri.trace.common.result.R;
import com.agri.trace.dto.ContractConfigDTO;
import com.agri.trace.dto.PageQueryDTO;
import com.agri.trace.entity.ChainEvidenceRecord;
import com.agri.trace.service.ChainService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chain")
public class ChainController {
    private final ChainService chainService;

    @GetMapping("/evidence/page")
    public R<IPage<ChainEvidenceRecord>> page(PageQueryDTO query) {
        return R.ok(chainService.page(query));
    }

    @GetMapping("/evidence/{id}")
    public R<ChainEvidenceRecord> detail(@PathVariable Long id) {
        return R.ok(chainService.detail(id));
    }

    @PostMapping("/evidence/{id}/verify")
    public R<ChainEvidenceRecord> verify(@PathVariable Long id) {
        return R.ok(chainService.verify(id));
    }

    @GetMapping("/contract/config")
    public R<Map<String, String>> config() {
        return R.ok(chainService.config());
    }

    @PutMapping("/contract/config")
    public R<Void> updateConfig(@RequestBody ContractConfigDTO dto) {
        chainService.updateConfig(dto);
        return R.ok();
    }
}
