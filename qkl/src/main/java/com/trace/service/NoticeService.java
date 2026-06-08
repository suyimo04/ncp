package com.trace.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.trace.common.PageResult;
import com.trace.dto.NoticeDTO;
import com.trace.entity.SysNotice;
import com.trace.exception.BusinessException;
import com.trace.mapper.SysNoticeMapper;
import com.trace.util.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 公告服务
 */
@Slf4j
@Service
public class NoticeService {

    @Autowired
    private SysNoticeMapper noticeMapper;

    /**
     * 分页查询公告
     */
    public PageResult<SysNotice> listNotices(Integer pageNum, Integer pageSize, Integer status, String keyword) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(SysNotice::getStatus, status);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SysNotice::getTitle, keyword);
        }

        // 非管理员只能看已发布的公告，且需要根据角色过滤
        String role = RequestContext.getCurrentRole();
        if (!"ADMIN".equals(role)) {
            wrapper.eq(SysNotice::getStatus, 1);
            wrapper.and(w -> w.eq(SysNotice::getTargetRole, "ALL")
                    .or().like(SysNotice::getTargetRole, role));
        }

        wrapper.orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> result = noticeMapper.selectPage(page, wrapper);
        return PageResult.of(result.getTotal(), result.getRecords(), result.getCurrent(), result.getSize());
    }

    /**
     * 公告详情
     */
    public SysNotice getById(Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) throw new BusinessException("公告不存在");
        return notice;
    }

    /**
     * 新增公告
     */
    public void addNotice(NoticeDTO dto) {
        SysNotice notice = new SysNotice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setNoticeType(dto.getNoticeType() != null ? dto.getNoticeType() : "SYSTEM");
        notice.setTargetRole(dto.getTargetRole() != null ? dto.getTargetRole() : "ALL");
        notice.setStatus(0); // 默认未发布
        notice.setPublisherId(RequestContext.getCurrentUserId());
        noticeMapper.insert(notice);
    }

    /**
     * 修改公告
     */
    public void updateNotice(NoticeDTO dto) {
        if (dto.getId() == null) throw new BusinessException("公告ID不能为空");
        SysNotice notice = noticeMapper.selectById(dto.getId());
        if (notice == null) throw new BusinessException("公告不存在");
        if (StrUtil.isNotBlank(dto.getTitle())) notice.setTitle(dto.getTitle());
        if (dto.getContent() != null) notice.setContent(dto.getContent());
        if (StrUtil.isNotBlank(dto.getNoticeType())) notice.setNoticeType(dto.getNoticeType());
        if (StrUtil.isNotBlank(dto.getTargetRole())) notice.setTargetRole(dto.getTargetRole());
        noticeMapper.updateById(notice);
    }

    /**
     * 删除公告
     */
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }

    /**
     * 发布/下线公告
     */
    public void changeStatus(Long id, Integer status) {
        SysNotice notice = noticeMapper.selectById(id);
        if (notice == null) throw new BusinessException("公告不存在");
        notice.setStatus(status);
        if (status == 1) {
            notice.setPublishTime(LocalDateTime.now());
        }
        noticeMapper.updateById(notice);
    }
}
