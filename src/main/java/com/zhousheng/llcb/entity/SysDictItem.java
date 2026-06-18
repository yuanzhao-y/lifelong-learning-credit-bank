package com.zhousheng.llcb.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.zhousheng.llcb.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_dict_item")
@EqualsAndHashCode(callSuper = true)
public class SysDictItem extends BaseEntity {
    private Long dictId;
    private String itemCode;
    private String itemName;
    private Integer sortNo;
    private String status;
    private String extraJson;
}
