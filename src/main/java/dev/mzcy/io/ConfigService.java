package dev.mzcy.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.mzcy.model.Config;

import java.io.IOException;
import java.nio.file.*;

public class ConfigService {
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    public Config load(Path file) throws IOException {
        if (Files.notExists(file)) return new Config();
        return yaml.readValue(Files.readString(file), Config.class);
    }

    public void save(Path file, Config config) throws IOException {
        if (Files.notExists(file.getParent())) Files.createDirectories(file.getParent());
        Files.writeString(file, yaml.writerWithDefaultPrettyPrinter().writeValueAsString(config));
    }
}