package com.example.imageprocesspip.mapper;

import com.example.imageprocesspip.entity.ImageLabel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ImageLabelRowMapper implements RowMapper<ImageLabel> {

    @Override
    public ImageLabel mapRow(ResultSet rs, int rowNum) throws SQLException {
        ImageLabel imageLabel = new ImageLabel();
        imageLabel.setImageId(rs.getString("image_id"));
        imageLabel.setLabelId(rs.getString("label_id"));
        return imageLabel;
    }
}