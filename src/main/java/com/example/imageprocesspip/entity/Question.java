package com.example.imageprocesspip.entity;

import com.example.imageprocesspip.enums.ChallengeType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Getter
@Setter
@Accessors(chain = true)
public class Question {

    String questionId;
    String labelId;
    ChallengeType type;

}
