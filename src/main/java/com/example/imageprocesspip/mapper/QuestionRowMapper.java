package com.example.imageprocesspip.mapper;

import com.example.imageprocesspip.entity.Question;
import com.example.imageprocesspip.enums.ChallengeType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QuestionRowMapper implements RowMapper<Question> {

    @Override
    public Question mapRow(ResultSet rs, int rowNum) throws SQLException {
        Question question = new Question();
        question.setQuestionId(rs.getString("id"));
        question.setLabelId(rs.getString("label_id"));

        // Assuming 'challenge_type' is stored as a String and there is a matching enum for it
        // This will need to be adjusted if your 'challenge_type' is stored differently
        int challengeTypeInt = rs.getInt("challenge_type");
        ChallengeType challengeType;
        try {
            challengeType = ChallengeType.getByValue(challengeTypeInt);
        } catch (IllegalArgumentException | NullPointerException e) {
            // Handle the case where the challenge type is not valid or null
            challengeType = ChallengeType.SINGLE_TILING; // Replace DEFAULT with a valid default enum
        }
        question.setType(challengeType);

        return question;
    }
}