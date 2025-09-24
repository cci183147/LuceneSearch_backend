package com.cci.lucenesearch.model;

public class Request {
    public String queryStr;
    public String startDate;
    public String endDate ;
    public Integer page = 0;
    public String sortField = "title";

    // 必须有无参构造函数供Spring反序列化
    public Request() {}
    // Getter和Setter方法（Spring反序列化需要）
    public String getQueryStr() { return queryStr; }
    public void setQueryStr(String queryStr) { this.queryStr = queryStr; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) {
        this.startDate = startDate != null ? startDate : "";
    }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) {
        this.endDate = endDate != null ? endDate : "";
    }

    public Integer getPage() { return page; }
    public void setPage(Integer page) {
        this.page = page != null ? page : 0;
    }

    public String getSortField() { return sortField; }
    public void setSortField(String sortField) {
        this.sortField = sortField != null ? sortField : "title";
    }
}