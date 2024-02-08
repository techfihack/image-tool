package com.example.imageprocesspip.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Getter
@Setter
@Accessors(chain = true)
public class ImageLabel {

    String imageId;
    String labelId;

}