# Auto Organizer (Swing)

[![Build](https://img.shields.io/badge/build-Gradle-blue)](./)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://adoptium.net/)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Windows%20|%20Linux%20|%20macOS-lightgrey)](#)

A simple **file auto-organizer** in Java (Swing). Watch a folder, define rules, and automatically move/copy/rename files based on glob/regex patterns.

---

## ✨ Features
- Watch any folder for new files (`WatchService`)
- Rules:
    - Glob or regex patterns
    - Target directories
    - Priority (for multiple matches)
    - MOVE or COPY actions
    - Date subfolders (`yyyy/MM`, etc.)
    - Rename templates (`${name}`, `${ext}`, `${date}`)
- **Dry-Run** mode
- Swing GUI: rule table, start/stop, log panel
- YAML config at `~/.auto-organizer/config.yml`

---

## 🛠 Requirements
- **Java 21+**
- **Gradle** (wrapper included)

---

## 🚀 Run

```bash
git clone https://github.com/YOURNAME/auto-organizer.git
cd auto-organizer
./gradlew run         # Windows: gradlew.bat run
```

The Swing app launches. Choose a watch folder, add rules, hit Start.

---

## 📂 Configuration

Location: `~/.auto-organizer/config.yml`
Example:

```yaml
rules:
  - name: PDF to Docs
    pattern: "*.pdf"
    patternType: glob
    targetDir: /home/user/Documents/PDF
    priority: 10
    action: MOVE
```

**Rename template variables:** `${name}`, `${ext}`, `${date}`
**Date format default:** `yyyy-MM-dd_HH-mm-ss` (when used in templates)

---

## 🖼 GUI Preview (textual)

* **Top bar:** Folder selector • Dry-Run toggle • Start/Stop
* **Table:** Name | Pattern | Type | Target | Priority
* **Log:** Shows actions and errors

---

## 🧪 Dev Tips

* Use `./gradlew clean run` if you change Lombok models
* Config is saved automatically when you edit rules or options

---

## 📌 Roadmap

* MIME type checks (`Files.probeContentType`)
* Initial scan for existing files
* Tray icon + background service
* Import/Export rules (YAML/JSON)

---

## 🤝 Contributing

PRs welcome! Please open an issue to discuss major changes.

---

## 📜 License

MIT License. See [LICENSE](LICENSE) for details.