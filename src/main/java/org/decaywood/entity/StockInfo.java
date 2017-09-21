package org.decaywood.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
@Data
@Table(name = "stockinfo")
public class StockInfo {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private String id;

    private String code;

    private String name;

    private String enName;

    private String hasexist;

    private String flag;

    private String type;

    private String stock_id;

    private String ind_id;

    private String ind_name;

}
