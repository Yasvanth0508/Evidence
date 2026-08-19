package com.example.backend.report.dto;

import java.util.List;

public class ReportListResponse {

    private List<ReportItemDto> reports;
    private long totalCount;
    private int page;
    private int size;

    public ReportListResponse() {
    }

    public ReportListResponse(List<ReportItemDto> reports, long totalCount, int page, int size) {
        this.reports = reports;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
    }

    public List<ReportItemDto> getReports() {
        return reports;
    }

    public void setReports(List<ReportItemDto> reports) {
        this.reports = reports;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
