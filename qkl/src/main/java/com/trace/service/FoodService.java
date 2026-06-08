package com.trace.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.FoodDTO;
import com.trace.entity.EnterpriseInfo;
import com.trace.entity.FoodBrand;
import com.trace.entity.FoodCategory;
import com.trace.entity.FoodInfo;
import com.trace.exception.BusinessException;
import com.trace.mapper.EnterpriseInfoMapper;
import com.trace.mapper.FoodBrandMapper;
import com.trace.mapper.FoodCategoryMapper;
import com.trace.mapper.FoodInfoMapper;
import com.trace.util.RequestContext;
import com.trace.vo.FoodVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 食品档案服务
 */
@Slf4j
@Service
public class FoodService {

    @Autowired
    private FoodInfoMapper foodInfoMapper;
    @Autowired
    private FoodCategoryMapper categoryMapper;
    @Autowired
    private FoodBrandMapper brandMapper;
    @Autowired
    private EnterpriseInfoMapper enterpriseInfoMapper;

    /**
     * 分页查询食品列表
     */
    public PageResult<FoodVO> listFoods(Integer pageNum, Integer pageSize,
                                         String keyword, Long categoryId, Long producerId) {
        Page<FoodInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FoodInfo> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(FoodInfo::getFoodName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(FoodInfo::getCategoryId, categoryId);
        }

        // 生产企业只能看自己的食品
        String role = RequestContext.getCurrentRole();
        if ("PRODUCER".equals(role)) {
            Long userId = RequestContext.getCurrentUserId();
            EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
            );
            if (ent != null) {
                wrapper.eq(FoodInfo::getProducerId, ent.getId());
            }
        } else if (producerId != null) {
            wrapper.eq(FoodInfo::getProducerId, producerId);
        }

        wrapper.orderByDesc(FoodInfo::getCreateTime);
        Page<FoodInfo> result = foodInfoMapper.selectPage(page, wrapper);
        List<FoodVO> voList = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), voList, result.getCurrent(), result.getSize());
    }

    /**
     * 食品详情
     */
    public FoodVO getById(Long id) {
        FoodInfo info = foodInfoMapper.selectById(id);
        if (info == null) throw new BusinessException("食品不存在");
        return toVO(info);
    }

    /**
     * 新增食品
     */
    public void addFood(FoodDTO dto) {
        FoodInfo food = new FoodInfo();
        BeanUtil.copyProperties(dto, food);

        // 自动关联当前登录用户的企业
        Long userId = RequestContext.getCurrentUserId();
        EnterpriseInfo ent = enterpriseInfoMapper.selectOne(
                new LambdaQueryWrapper<EnterpriseInfo>().eq(EnterpriseInfo::getUserId, userId)
        );
        if (ent == null) {
            throw new BusinessException("请先完善企业信息");
        }
        food.setProducerId(ent.getId());
        food.setStatus(1);
        foodInfoMapper.insert(food);
        log.info("新增食品: {}", dto.getFoodName());
    }

    /**
     * 修改食品
     */
    public void updateFood(FoodDTO dto) {
        if (dto.getId() == null) throw new BusinessException("食品ID不能为空");
        FoodInfo food = foodInfoMapper.selectById(dto.getId());
        if (food == null) throw new BusinessException("食品不存在");

        if (StrUtil.isNotBlank(dto.getFoodName())) food.setFoodName(dto.getFoodName());
        if (dto.getCategoryId() != null) food.setCategoryId(dto.getCategoryId());
        if (dto.getBrandId() != null) food.setBrandId(dto.getBrandId());
        if (StrUtil.isNotBlank(dto.getSpecification())) food.setSpecification(dto.getSpecification());
        if (StrUtil.isNotBlank(dto.getUnit())) food.setUnit(dto.getUnit());
        if (dto.getShelfLife() != null) food.setShelfLife(dto.getShelfLife());
        if (StrUtil.isNotBlank(dto.getStorageCondition())) food.setStorageCondition(dto.getStorageCondition());
        if (StrUtil.isNotBlank(dto.getFoodImage())) food.setFoodImage(dto.getFoodImage());
        if (StrUtil.isNotBlank(dto.getDescription())) food.setDescription(dto.getDescription());

        foodInfoMapper.updateById(food);
    }

    /**
     * 删除食品
     */
    public void deleteFood(Long id) {
        foodInfoMapper.deleteById(id);
    }

    /**
     * 实体转VO，补充分类名、品牌名、企业名
     */
    private FoodVO toVO(FoodInfo info) {
        FoodVO vo = new FoodVO();
        BeanUtil.copyProperties(info, vo);
        // 分类名
        if (info.getCategoryId() != null) {
            FoodCategory cat = categoryMapper.selectById(info.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getCategoryName());
        }
        // 品牌名
        if (info.getBrandId() != null) {
            FoodBrand brand = brandMapper.selectById(info.getBrandId());
            if (brand != null) vo.setBrandName(brand.getBrandName());
        }
        // 企业名
        if (info.getProducerId() != null) {
            EnterpriseInfo ent = enterpriseInfoMapper.selectById(info.getProducerId());
            if (ent != null) vo.setProducerName(ent.getEnterpriseName());
        }
        return vo;
    }
}
