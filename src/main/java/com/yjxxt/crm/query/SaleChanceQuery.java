package com.yjxxt.crm.query;

import com.yjxxt.crm.base.BaseQuery;

/**
 * 营销机会管理多条件查询条件
 */
public class SaleChanceQuery extends BaseQuery {

    //客户名名称
    private String customerName;
    private String createMan;
    private String state;

    public SaleChanceQuery() {
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCreateMan() {
        return createMan;
    }

    public void setCreateMan(String createMan) {
        this.createMan = createMan;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

