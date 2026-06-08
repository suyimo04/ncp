package com.trace.controller;

import com.trace.common.Result;
import com.trace.entity.FoodCategory;
import com.trace.service.CategoryService;
import com.trace.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食品分类接口
 */
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /** 分类列表 */
    @GetMapping("/list")
    public Result<List<FoodCategory>> list() {
        return Result.success(categoryService.listAll());
    }

    /** 分类树 */
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    /** 新增分类 */
    @PostMapping
    public Result<Void> add(@RequestBody FoodCategory category) {
        categoryService.add(category);
        return Result.success();
    }

    /** 修改分类 */
    @PutMapping
    public Result<Void> update(@RequestBody FoodCategory category) {
        categoryService.update(category);
        return Result.success();
    }

    /** 删除分类 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
