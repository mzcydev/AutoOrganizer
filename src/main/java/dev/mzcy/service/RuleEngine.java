package dev.mzcy.service;

import dev.mzcy.model.Rule;
import lombok.RequiredArgsConstructor;

import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class RuleEngine {
    private final List<Rule> rules;

    public Optional<Rule> match(Path file) {
        String name = file.getFileName().toString();
        return rules.stream()
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .filter(r -> matches(r, name))
                .findFirst();
    }

    private boolean matches(Rule r, String name) {
        if ("glob".equalsIgnoreCase(r.getPatternType())) {
            PathMatcher m = FileSystems.getDefault().getPathMatcher("glob:" + r.getPattern());
            return m.matches(Path.of(name));
        } else {
            return Pattern.compile(r.getPattern()).matcher(name).matches();
        }
    }
}