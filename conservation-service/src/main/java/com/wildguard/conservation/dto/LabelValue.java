package com.wildguard.conservation.dto;

// A name with a number, used for charts (for example "Rehabilitation" -> 1).
public class LabelValue {

    private String label;
    private long value;

    public LabelValue(String label, long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }
}
