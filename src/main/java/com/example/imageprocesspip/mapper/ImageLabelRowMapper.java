package com.example.imageprocesspip.mapper;

import com.example.imageprocesspip.entity.ImageLabel;
import com.example.imageprocesspip.enums.ChallengeType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ImageLabelRowMapper implements RowMapper<ImageLabel> {

    @Override
    public ImageLabel mapRow(ResultSet rs, int rowNum) throws SQLException {
        ImageLabel imageLabel = new ImageLabel();
        imageLabel.setImageId(rs.getString("image_id"));
        imageLabel.setLabelId(rs.getString("label_id"));

        int challengeTypeInt = rs.getInt("challenge_type");
        ChallengeType challengeType;
        try {
            challengeType = ChallengeType.getByValue(challengeTypeInt);
        } catch (IllegalArgumentException | NullPointerException e) {
            // Handle the case where the challenge type is not valid or null
            challengeType = ChallengeType.SINGLE_TILING; // Replace DEFAULT with a valid default enum
        }
        imageLabel.setChallengeType(challengeType);
        return imageLabel;
    }
}