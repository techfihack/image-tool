package com.example.imageprocesspip.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ImageFormat {

    WEBP("webp"),
    JPEG ("jpeg");

    final String value;

    ImageFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<String> getAllValues() {
        return Arrays.stream(ImageFormat.values())
                .map(ImageFormat::getValue)
                .collect(Collectors.toList());
    }
}
