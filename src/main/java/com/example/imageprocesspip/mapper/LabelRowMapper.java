package com.example.imageprocesspip.mapper;

import com.example.imageprocesspip.entity.Label;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LabelRowMapper implements RowMapper<Label> {

    @Override
    public Label mapRow(ResultSet rs, int rowNum) throws SQLException {
        Label label = new Label();
        label.setLabelId(rs.getString("id"));
        label.setLabelName(rs.getString("label_name"));
        return label;
    }
}