package com.myapp.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;
import java.io.Serializable;
import java.sql.Date;

@Data
@TableName("netem_learned_detail")
public class NetemLearnedDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer netemLearnedId;

    private Date netemLearnedDate;
}
