package dev.mzcy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    private Path watchDir;
    @Builder.Default
    private boolean dryRun = true;
    @Builder.Default
    private boolean autoStart = false;
    @Builder.Default
    private List<Rule> rules = List.of();
}
