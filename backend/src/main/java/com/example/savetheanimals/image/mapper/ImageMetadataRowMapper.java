package com.example.savetheanimals.image.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import com.example.savetheanimals.image.model.Animal;
import com.example.savetheanimals.image.model.ImageMetadata;

import org.springframework.jdbc.core.RowMapper;

public final class ImageMetadataRowMapper implements RowMapper<ImageMetadata> {

    @Override
    public ImageMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ImageMetadata(
                rs.getLong("id"),
                Animal.valueOf(rs.getString("animal")),
                rs.getString("content_type"),
                rs.getString("source_url"),
                rs.getLong("size_bytes"),
                Instant.parse(rs.getString("created_at")));
    }
}
