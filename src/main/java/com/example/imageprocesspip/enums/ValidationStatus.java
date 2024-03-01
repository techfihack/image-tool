package com.example.imageprocesspip.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ValidationStatus {

    EXPIRED("EXPIRED"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private String desc;

    ValidationStatus(String desc){
        this.desc = desc;
    }

    @JsonValue
    public String getDesc() { return desc; }
}
