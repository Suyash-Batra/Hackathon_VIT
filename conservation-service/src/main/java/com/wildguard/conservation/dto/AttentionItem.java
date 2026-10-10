package com.wildguard.conservation.dto;

// One alert for the dashboard, for example "release review waiting for RES-028".
public class AttentionItem {

    private String category;
    private String level;
    private String title;
    private String detail;
    private String refType;
    private Long refId;

    public AttentionItem(String category, String level, String title, String detail, String refType, Long refId) {
        this.category = category;
        this.level = level;
        this.title = title;
        this.detail = detail;
        this.refType = refType;
        this.refId = refId;
    }

    public String getCategory() {
        return category;
    }

    public String getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getRefType() {
        return refType;
    }

    public Long getRefId() {
        return refId;
    }
}
