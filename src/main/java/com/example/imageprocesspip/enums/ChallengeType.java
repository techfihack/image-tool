package com.example.imageprocesspip.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChallengeType {

    SINGLE_TILING(0),
    MULTI_TILING(1),
    IMAGE_TYPING(2),
    IMAGE_LABELLING(3);

    private int value;

    ChallengeType(int value){
        this.value = value;
    }

    @JsonValue
    public int getCode() { return value; }

    public static ChallengeType getByValue(int value) {
        for (ChallengeType e : values()) {
            if(e.getCode() == value){
                return e;
            }
        }
        return null;
    }

}
