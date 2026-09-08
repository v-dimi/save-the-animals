package com.example.savetheanimals.image.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.example.savetheanimals.image.model.ImageContent;

import org.springframework.jdbc.core.RowMapper;

public final class ImageContentRowMapper implements RowMapper<ImageContent> {

    @Override
    public ImageContent mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ImageContent(
                rs.getBytes("data"),
                rs.getString("content_type"));
    }
}
