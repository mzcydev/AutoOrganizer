package dev.mzcy.service;

import dev.mzcy.model.Rule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class Organizer {
    private final boolean dryRun;

    public Organizer(boolean dryRun) { this.dryRun = dryRun; }

    public Path apply(Path file, Rule r) throws IOException {
        Path targetBase = r.getTargetDir();
        if (r.getDateSubfolder()!=null && !r.getDateSubfolder().isBlank()) {
            String sub = DateTimeFormatter.ofPattern(r.getDateSubfolder()).format(LocalDateTime.now());
            targetBase = targetBase.resolve(sub);
        }
        Files.createDirectories(targetBase);

        String newName = r.getRenameTemplate()==null || r.getRenameTemplate().isBlank()
                ? file.getFileName().toString()
                : renderName(file, r.getRenameTemplate());
        Path target = resolveNonClashing(targetBase.resolve(newName));

        log.info("{} {} -> {}", r.getAction(), file, target);
        if (!dryRun) {
            if ("MOVE".equalsIgnoreCase(r.getAction())) {
                Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return target;
    }

    private Path resolveNonClashing(Path target) {
        if (Files.notExists(target)) return target;
        String fn = target.getFileName().toString();
        String base = fn.contains(".") ? fn.substring(0, fn.lastIndexOf('.')) : fn;
        String ext  = fn.contains(".") ? fn.substring(fn.lastIndexOf('.')) : "";
        int i=1;
        Path parent = target.getParent();
        while (Files.exists(parent.resolve(base + "_" + i + ext))) i++;
        return parent.resolve(base + "_" + i + ext);
    }

    private String renderName(Path file, String tpl) {
        String name = file.getFileName().toString();
        String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
        String ext  = name.contains(".") ? name.substring(name.lastIndexOf('.')+1) : "";
        String now  = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss").format(LocalDateTime.now());
        return tpl
                .replace("${name}", base)
                .replace("${ext}", ext)
                .replace("${date}", now);
    }
}