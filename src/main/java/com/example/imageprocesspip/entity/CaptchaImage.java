package com.example.imageprocesspip.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Getter
@Setter
@Accessors(chain = true)
public class CaptchaImage {

    // base 64 converted image slices string
    private List<String> imageSlices;

    private List<String> temporaryIds;

    private String filePath;

    private String temporaryId;

}
