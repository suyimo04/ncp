package com.trace.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.entity.FoodBrand;
import com.trace.exception.BusinessException;
import com.trace.mapper.FoodBrandMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 品牌管理服务
 */
@Slf4j
@Service
public class BrandService {

    @Autowired
    private FoodBrandMapper brandMapper;

    /**
     * 品牌分页列表
     */
    public PageResult<FoodBrand> listBrands(Integer pageNum, Integer pageSize, String keyword) {
        Page<FoodBrand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodBrand> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(FoodBrand::getBrandName, keyword);
        }
        wrapper.orderByDesc(FoodBrand::getCreateTime);
        Page<FoodBrand> result = brandMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取全部品牌（下拉选择用）
     */
    public List<FoodBrand> listAll() {
        return brandMapper.selectList(
                new LambdaQueryWrapper<FoodBrand>().eq(FoodBrand::getStatus, 1)
        );
    }

    public void add(FoodBrand brand) {
        if (brand.getStatus() == null) brand.setStatus(1);
        brandMapper.insert(brand);
    }

    public void update(FoodBrand brand) {
        if (brand.getId() == null) throw new BusinessException("品牌ID不能为空");
        brandMapper.updateById(brand);
    }

    public void delete(Long id) {
        brandMapper.deleteById(id);
    }
}
