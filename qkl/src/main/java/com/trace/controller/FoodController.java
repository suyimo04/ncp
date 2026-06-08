package com.trace.controller;

import com.trace.common.PageResult;
import com.trace.common.Result;
import com.trace.dto.FoodDTO;
import com.trace.service.FoodService;
import com.trace.vo.FoodVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 食品档案接口
 */
@RestController
@RequestMapping("/api/food")
public class FoodController {

    @Autowired
    private FoodService foodService;

    /** 食品列表 */
    @GetMapping("/list")
    public Result<PageResult<FoodVO>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long producerId) {
        return Result.success(foodService.listFoods(pageNum, pageSize, keyword, categoryId, producerId));
    }

    /** 食品详情 */
    @GetMapping("/{id}")
    public Result<FoodVO> detail(@PathVariable Long id) {
        return Result.success(foodService.getById(id));
    }

    /** 新增食品 */
    @PostMapping
    public Result<Void> add(@RequestBody FoodDTO dto) {
        foodService.addFood(dto);
        return Result.success();
    }

    /** 修改食品 */
    @PutMapping
    public Result<Void> update(@RequestBody FoodDTO dto) {
        foodService.updateFood(dto);
        return Result.success();
    }

    /** 删除食品 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        foodService.deleteFood(id);
        return Result.success();
    }
}
