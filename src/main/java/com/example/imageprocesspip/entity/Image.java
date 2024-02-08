package com.example.imageprocesspip.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Data
@Accessors(chain = true)
public class Image {

    private String imageId;
    private String imageName;
    private byte[] imageData;
    private int section;
    private String groupId;     // Added to group slices with their original image
    private int isOriginal;     // boolean should be mapped to int if the database column is TINYINT or similar
}









