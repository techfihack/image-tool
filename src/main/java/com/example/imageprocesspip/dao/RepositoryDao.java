package com.example.imageprocesspip.dao;

import com.example.imageprocesspip.entity.Image;
import com.example.imageprocesspip.entity.Question;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class RepositoryDao {

    private final JdbcTemplate jdbcTemplate;

    public RepositoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveImages(Image image) {
        String sql = "INSERT INTO images (id, image_name, image_data, section, group_id, is_original) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                image.getImageId(),
                image.getImageName(),
                image.getImageData(),
                image.getSection(),
                image.getGroupId(),
                image.getIsOriginal()
        );
    }

    public void saveQuestions(Question question) {
        String sql = "INSERT INTO questions (id, challenge_type, label_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql,
                question.getQuestionId(),
                question.getType().getCode(),
                question.getLabelId()
        );
    }


    // Method to insert a label into the database if it doesn't already exist
    public String saveLabelToDatabaseIfNotExists(String label) {
        // First, check if the label already exists
        String labelId = jdbcTemplate.queryForObject(
                "SELECT id FROM labels WHERE label_name = ?",
                new Object[]{label},
                String.class
        );

        // If the label doesn't exist, insert it and return the new ID
        if (labelId == null) {
            labelId = UUID.randomUUID().toString().replace("-", "");
            jdbcTemplate.update(
                    "INSERT INTO labels (id, label_name) VALUES (?, ?)",
                    labelId, label
            );
        }
        return labelId;
    }


    // Method to create an entry in the image_labels join table
    public void saveImageLabelRelationToDatabase(String imageId, String labelId) {
        jdbcTemplate.update(
                "INSERT INTO image_labels (image_id, label_id) VALUES (?, ?)",
                imageId, labelId
        );
    }

    public String getLabelIdFromDatabase(String label) {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM labels WHERE label_name = ?",
                    new Object[]{label},
                    String.class
            );
    }

    public void saveQuestionToDatabase(String labelId, int challengeType) {
        jdbcTemplate.update(
                "INSERT INTO questions (id, challenge_type, label_id) VALUES (?, ?, ?)",
                UUID.randomUUID().toString().replace("-", ""),
                challengeType,
                labelId
        );
    }


}