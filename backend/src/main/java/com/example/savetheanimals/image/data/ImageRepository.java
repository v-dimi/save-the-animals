package com.example.savetheanimals.image.data;

import java.util.Arrays;
import java.util.Optional;

import com.example.savetheanimals.image.mapper.ImageContentRowMapper;
import com.example.savetheanimals.image.mapper.ImageMetadataRowMapper;
import com.example.savetheanimals.image.model.ImageContent;
import com.example.savetheanimals.image.model.ImageMetadata;
import com.example.savetheanimals.image.model.NewImage;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ImageRepository {

    private static final String INSERT = """
            INSERT INTO stored_image (animal, content_type, source_url, size_bytes, data, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    /**
     * Ordered by id, not by created_at
     * blob column is intentionally not selected.
     */
    private static final String SELECT_LATEST_METADATA = """
            SELECT id, animal, content_type, source_url, size_bytes, created_at
            FROM stored_image
            ORDER BY id DESC
            LIMIT 1
            """;

    private static final String SELECT_CONTENT_BY_ID = """
            SELECT data, content_type
            FROM stored_image
            WHERE id = ?
            """;

    // Stateless, so one instance of each is enough.
    private static final RowMapper<ImageMetadata> METADATA_MAPPER = new ImageMetadataRowMapper();
    private static final RowMapper<ImageContent> CONTENT_MAPPER = new ImageContentRowMapper();

    private final JdbcClient jdbcClient;

    ImageRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public ImageMetadata save(NewImage image) {
        var keyHolder = new GeneratedKeyHolder();

        jdbcClient.sql(INSERT)
                .params(Arrays.asList(
                        image.animal().name(),
                        image.contentType(),
                        image.sourceUrl(),
                        image.sizeBytes(),
                        image.data(),
                        image.createdAt().toString()))
                .update(keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId == null) {
            throw new IllegalStateException("Insert into stored_image returned no generated id.");
        }

        return new ImageMetadata(
                generatedId.longValue(),
                image.animal(),
                image.contentType(),
                image.sourceUrl(),
                image.sizeBytes(),
                image.createdAt());
    }

    public Optional<ImageMetadata> findLatestMetadata() {
        return jdbcClient.sql(SELECT_LATEST_METADATA)
                .query(METADATA_MAPPER)
                .optional();
    }

    public Optional<ImageContent> findContentById(long id) {
        return jdbcClient.sql(SELECT_CONTENT_BY_ID)
                .param(id)
                .query(CONTENT_MAPPER)
                .optional();
    }
}
