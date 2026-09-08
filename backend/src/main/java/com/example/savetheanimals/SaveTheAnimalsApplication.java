package com.example.savetheanimals;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SaveTheAnimalsApplication {

    private static final String DEFAULT_DATABASE_PATH = "./data/app.db";

    public static void main(String[] args) {
        ensureDatabaseDirectoryExists();
        SpringApplication.run(SaveTheAnimalsApplication.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    private static void ensureDatabaseDirectoryExists() {
        String databasePath = System.getenv().getOrDefault("APP_DB_PATH", DEFAULT_DATABASE_PATH);
        Path directory = Path.of(databasePath).toAbsolutePath().getParent();
        if (directory == null) {
            return;
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not create the database directory " + directory, ex);
        }
    }
}
