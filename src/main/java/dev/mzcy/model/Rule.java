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
public class Rule {
    private String name;
    /**
     * Glob- oder Regex-Muster (z. B. "*.pdf" oder "(?i).*\\.(png|jpg)")
     */
    private String pattern;
    /**
     * "glob" oder "regex"
     */
    @Builder.Default
    private String patternType = "glob";
    /**
     * Zielordner
     */
    private Path targetDir;
    /**
     * Optional: in Unterordner nach Datum ablegen: yyyy/MM
     */
    @Builder.Default
    private String dateSubfolder = "yyyy/MM";
    /**
     * Aktion: MOVE|COPY
     */
    @Builder.Default
    private String action = "MOVE";
    /**
     * Optional: umbenennen mit Template, z. B. "${date:yyyy-MM-dd}_${name}"
     */
    private String renameTemplate;
    /**
     * Nur anwenden, wenn Datei > X KB (0 = egal)
     */
    @Builder.Default
    private long minSizeKb = 0;
    /**
     * Priorität (kleiner = früher)
     */
    @Builder.Default
    private int priority = 100;
    /**
     * Liste erlaubter MIME-Typen (leer = egal)
     */
    @Builder.Default
    private List<String> mimeWhitelist = List.of();
}
