package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.trace.entity.FoodCategory;
import com.trace.exception.BusinessException;
import com.trace.mapper.FoodCategoryMapper;
import com.trace.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 食品分类服务
 */
@Slf4j
@Service
public class CategoryService {

    @Autowired
    private FoodCategoryMapper categoryMapper;

    /**
     * 获取全部分类列表
     */
    public List<FoodCategory> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<FoodCategory>().orderByAsc(FoodCategory::getSort)
        );
    }

    /**
     * 获取分类树
     */
    public List<CategoryVO> tree() {
        List<FoodCategory> all = listAll();
        List<CategoryVO> voList = all.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            BeanUtil.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构
        return buildTree(voList, 0L);
    }

    private List<CategoryVO> buildTree(List<CategoryVO> allList, Long parentId) {
        List<CategoryVO> children = new ArrayList<>();
        for (CategoryVO vo : allList) {
            if (parentId.equals(vo.getParentId())) {
                vo.setChildren(buildTree(allList, vo.getId()));
                children.add(vo);
            }
        }
        return children;
    }

    /**
     * 新增分类
     */
    public void add(FoodCategory category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
    }

    /**
     * 修改分类
     */
    public void update(FoodCategory category) {
        if (category.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        categoryMapper.updateById(category);
    }

    /**
     * 删除分类
     */
    public void delete(Long id) {
        // 检查是否有子分类
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<FoodCategory>().eq(FoodCategory::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
