package com.agri.trace.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pub_query_record")
public class PubQueryRecord extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String certificateCode;
    private String queryIp;
    private Integer queryResult;
}
