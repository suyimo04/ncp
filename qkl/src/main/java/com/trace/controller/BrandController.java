package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.entity.FoodBrand;
import com.trace.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌管理接口
 */
@RestController
@RequestMapping("/api/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /** 品牌分页列表 */
    @GetMapping("/list")
    public Result<PageResult<FoodBrand>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(brandService.listBrands(pageNum, pageSize, keyword));
    }

    /** 全部品牌（下拉选择） */
    @GetMapping("/all")
    public Result<List<FoodBrand>> all() {
        return Result.success(brandService.listAll());
    }

    /** 新增品牌 */
    @PostMapping
    public Result<Void> add(@RequestBody FoodBrand brand) {
        brandService.add(brand);
        return Result.success();
    }

    /** 修改品牌 */
    @PutMapping
    public Result<Void> update(@RequestBody FoodBrand brand) {
        brandService.update(brand);
        return Result.success();
    }

    /** 删除品牌 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return Result.success();
    }
}
