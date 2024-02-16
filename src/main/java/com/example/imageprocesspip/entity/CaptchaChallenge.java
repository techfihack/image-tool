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
public class CaptchaChallenge {

    private String sessionId;
    private CaptchaImage captchaImages;
    private String questionString;
    private int challengeType;

}
