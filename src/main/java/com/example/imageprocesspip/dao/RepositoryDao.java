package com.example.imageprocesspip.dao;

import com.example.imageprocesspip.entity.Image;
import com.example.imageprocesspip.entity.ImageLabel;
import com.example.imageprocesspip.entity.Label;
import com.example.imageprocesspip.entity.Question;
import com.example.imageprocesspip.mapper.ImageLabelRowMapper;
import com.example.imageprocesspip.mapper.ImageRowMapper;
import com.example.imageprocesspip.mapper.LabelRowMapper;
import com.example.imageprocesspip.mapper.QuestionRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class RepositoryDao {

    private final JdbcTemplate jdbcTemplate;

    public RepositoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveImages(Image image) {
        String sql = "INSERT INTO images (id, image_name, image_path, section, group_id, is_original) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                image.getImageId(),
                image.getImageName(),
                image.getImagePath(),
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
    public String saveLabelToDatabaseIfNotExists(String label, String labelId) {
        String selectSql = "SELECT id FROM labels WHERE label_name = ?";
        List<String> labelIds = jdbcTemplate.query(
                selectSql,
                new Object[]{label},
                (rs, rowNum) -> rs.getString("id")
        );

        // If labelIds is empty, no label was found, so insert a new one.
        if (labelIds.isEmpty()) {
            jdbcTemplate.update(
                    "INSERT INTO labels (id, label_name) VALUES (?, ?)",
                    labelId, label
            );
            return labelId;
        } else {
            // Return the existing label ID
            return labelIds.get(0);
        }
    }


    // Method to create an entry in the image_labels join table
    public void saveImageLabelRelationToDatabase(String imageId, String labelId, int challengeType) {
        jdbcTemplate.update(
                "INSERT INTO image_labels (image_id, label_id, challenge_type) VALUES (?, ?, ?)",
                imageId, labelId, challengeType
        );
    }

    public String getLabelIdByName(String label) {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM labels WHERE label_name = ?",
                    new Object[]{label},
                    String.class
            );
    }

    public Label getLabelById(String labelId) {
        String sql = "SELECT id, label_name FROM labels WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{labelId}, new LabelRowMapper());
    }


    public List<ImageLabel> getImageLabelsByLabelId(String labelId){
        String sql = "SELECT * FROM image_labels WHERE label_id = ?";
        return jdbcTemplate.query(sql, new Object[]{labelId}, new ImageLabelRowMapper());
    }

    public List<Image>  getImageSlicesGroupById(String imageId){
        String sql = "select * from images where group_id = ( select group_id from images where id = ? ) and is_original = 0 order by section ASC";
        return jdbcTemplate.query(sql, new Object[]{imageId}, new ImageRowMapper());
    }

    public Image getAnswerImageById(String imageId){
        String sql = "select * from images where id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{imageId}, new ImageRowMapper());
    }


    public List<String> getSameGroupAnswerImages(String labelId, List<String> imageIds){

        // Prepare the SQL query with placeholders for labelId and imageIds
        String sql = "SELECT image_id FROM image_labels WHERE label_id = ? AND image_id IN (" +
                String.join(",", Collections.nCopies(imageIds.size(), "?")) + ")";

        System.out.println("sql is " + sql);

        // Prepare the parameters for the SQL query
        List<Object> parameters = new ArrayList<>();
        parameters.add(labelId);
        parameters.addAll(imageIds);

        // Execute the query and return the list of image IDs
        return jdbcTemplate.query(sql, parameters.toArray(), (rs, rowNum) -> rs.getString("image_id"));
    }

    public List<String> getSameImageOtherLabelId(String imageId) {
        String sql = "SELECT label_id FROM image_labels WHERE image_id = ?";
        return jdbcTemplate.query(sql, new Object[]{imageId}, (rs, rowNum) -> rs.getString("label_id"));
    }

    public List<String> getLabelNameInId(List<String> labelIds){

        // Create a comma-separated string of placeholders
        String sql = "SELECT label_name FROM labels WHERE id IN (" + String.join(",", Collections.nCopies(labelIds.size(), "?")) + ")";

        // Convert ids list to an array of objects
        Object[] params = labelIds.toArray(new Object[0]);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("label_name"));
    }

    public List<String> getLabelNameNotInId(List<String> labelIds){

        // Create a comma-separated string of placeholders
        String sql = "SELECT label_name FROM labels WHERE id NOT IN (" + String.join(",", Collections.nCopies(labelIds.size(), "?")) + ") ORDER BY RAND() LIMIT 4";

        // Convert ids list to an array of objects
        Object[] params = labelIds.toArray(new Object[0]);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> rs.getString("label_name"));
    }

    public void saveQuestionToDatabase(String labelId, int challengeType) {
        jdbcTemplate.update(
                "INSERT INTO questions (id, challenge_type, label_id) VALUES (?, ?, ?)",
                UUID.randomUUID().toString().replace("-", ""),
                challengeType,
                labelId
        );
    }

    public Question getRandomQuestion() {
        String sql = "SELECT id, challenge_type, label_id FROM questions ORDER BY RAND() LIMIT 1";
        return jdbcTemplate.queryForObject(sql, new QuestionRowMapper());
    }

    public Question getQuestionByChallengeType(int type) {
        String sql = "SELECT id, challenge_type, label_id FROM questions WHERE challenge_type = ? ORDER BY RAND() LIMIT 1";
        return jdbcTemplate.queryForObject(sql, new Object[]{type}, new QuestionRowMapper());
    }



}