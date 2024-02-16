package com.example.imageprocesspip.mapper;

import com.example.imageprocesspip.entity.Image;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ImageRowMapper implements RowMapper<Image> {

    @Override
    public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
        Image image = new Image();
        image.setImageId(rs.getString("id"));
        image.setImageName(rs.getString("image_name"));
        image.setImagePath(rs.getString("image_path"));
        image.setSection(rs.getInt("section"));
        image.setGroupId(rs.getString("group_id"));
        image.setIsOriginal(rs.getInt("is_original"));
        return image;
    }
}