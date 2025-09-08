package dev.mzcy;

import dev.mzcy.io.ConfigService;
import dev.mzcy.model.Config;
import dev.mzcy.model.Rule;
import dev.mzcy.service.Organizer;
import dev.mzcy.service.RuleEngine;
import dev.mzcy.service.WatcherServiceEx;
import dev.mzcy.ui.RuleTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.Comparator;

public class AutoOrganizerApplication {

    private final ConfigService cfgIO = new ConfigService();
    private final Path cfgFile = Path.of(System.getProperty("user.home"), ".auto-organizer/config.yml");
    private Config cfg = new Config();
    private JFrame frame;
    private JLabel watchDirLbl;
    private JCheckBox dryRunChk;
    private JButton startBtn, stopBtn, chooseBtn, addBtn, rmBtn;
    private JTextArea logArea;
    private JTable ruleTable;
    private RuleTableModel ruleModel;

    private WatcherServiceEx watcher;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AutoOrganizerApplication().init());
    }

    private void init() {
        try {
            cfg = cfgIO.load(cfgFile);
        } catch (Exception ignored) {}

        frame = new JFrame("Auto Organizer (Swing)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 600);
        frame.setLocationRelativeTo(null);

        var root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.setContentPane(root);

        // Top controls
        var top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        watchDirLbl = new JLabel(cfg.getWatchDir() != null ? cfg.getWatchDir().toString() : "-");
        chooseBtn = new JButton("Select Watch Folder");
        chooseBtn.addActionListener(e -> chooseDir());
        dryRunChk = new JCheckBox("Dry-Run", cfg.isDryRun());
        startBtn = new JButton("Start");
        stopBtn = new JButton("Stop");
        stopBtn.setEnabled(false);
        startBtn.addActionListener(e -> startWatching());
        stopBtn.addActionListener(e -> stopWatching());
        top.add(new JLabel("Folder:"));
        top.add(watchDirLbl);
        top.add(chooseBtn);
        top.add(dryRunChk);
        top.add(startBtn);
        top.add(stopBtn);
        root.add(top, BorderLayout.NORTH);

        // Table
        ruleModel = new RuleTableModel();
        if (cfg.getRules() != null) cfg.getRules().stream()
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .forEach(ruleModel::addRule);
        ruleTable = new JTable(ruleModel);
        ruleTable.setFillsViewportHeight(true);
        var tablePane = new JScrollPane(ruleTable);

        addBtn = new JButton("Add Rule");
        rmBtn = new JButton("Remove Rule");
        addBtn.addActionListener(e -> addRuleDialog());
        rmBtn.addActionListener(e -> {
            int row = ruleTable.getSelectedRow();
            if (row >= 0) {
                ruleModel.removeAt(row);
                cfg.setRules(ruleModel.getRules());
                saveConfig();
            }
        });
        var tableBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tableBtns.add(addBtn);
        tableBtns.add(rmBtn);

        var center = new JPanel(new BorderLayout(8, 8));
        center.add(tablePane, BorderLayout.CENTER);
        center.add(tableBtns, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        // Log
        logArea = new JTextArea(6, 80);
        logArea.setEditable(false);
        var logPane = new JScrollPane(logArea);
        root.add(logPane, BorderLayout.SOUTH);

        frame.setVisible(true);

        if (cfg.isAutoStart() && cfg.getWatchDir() != null) startWatching();
    }

    private void chooseDir() {
        var fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = fc.showOpenDialog(frame);
        if (res == JFileChooser.APPROVE_OPTION) {
            var dir = fc.getSelectedFile().toPath();
            cfg.setWatchDir(dir);
            watchDirLbl.setText(dir.toString());
            saveConfig();
        }
    }

    private void addRuleDialog() {
        JTextField tfName = new JTextField();
        JTextField tfPat = new JTextField("*.pdf");
        JComboBox<String> cbType = new JComboBox<>(new String[]{"glob", "regex"});
        cbType.setSelectedItem("glob");
        JTextField tfTarget = new JTextField();
        JButton btnTarget = new JButton("…");
        btnTarget.addActionListener(e -> {
            var fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                tfTarget.setText(fc.getSelectedFile().toString());
            }
        });
        JSpinner spPrio = new JSpinner(new SpinnerNumberModel(100, 1, 999, 1));

        var panel = new JPanel(new GridBagLayout());
        var gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;
        panel.add(new JLabel("Name"), gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(tfName, gc);
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        panel.add(new JLabel("Pattern"), gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(tfPat, gc);
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0;
        panel.add(new JLabel("Type"), gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(cbType, gc);
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        panel.add(new JLabel("Target"), gc);
        var targetPanel = new JPanel(new BorderLayout(5, 0));
        targetPanel.add(tfTarget, BorderLayout.CENTER);
        targetPanel.add(btnTarget, BorderLayout.EAST);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(targetPanel, gc);
        gc.gridx = 0; gc.gridy = 4; gc.weightx = 0;
        panel.add(new JLabel("Priority"), gc);
        gc.gridx = 1; gc.weightx = 1;
        panel.add(spPrio, gc);

        int res = JOptionPane.showConfirmDialog(frame, panel, "New Rule", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            Rule r = Rule.builder()
                    .name(tfName.getText())
                    .pattern(tfPat.getText())
                    .patternType((String) cbType.getSelectedItem())
                    .targetDir(Path.of(tfTarget.getText()))
                    .priority((Integer) spPrio.getValue())
                    .build();
            ruleModel.addRule(r);
            cfg.setRules(ruleModel.getRules());
            saveConfig();
        }
    }

    private void startWatching() {
        if (cfg.getWatchDir() == null) {
            appendLog("Please select a watch folder first.");
            return;
        }
        try {
            stopWatching();
            cfg.setDryRun(dryRunChk.isSelected());
            saveConfig();
            var engine = new RuleEngine(cfg.getRules());
            var org = new Organizer(cfg.isDryRun());
            watcher = new WatcherServiceEx(cfg.getWatchDir(), path -> {
                SwingUtilities.invokeLater(() -> appendLog("New: " + path.getFileName()));
                engine.match(path).ifPresentOrElse(rule -> {
                    try {
                        var target = org.apply(path, rule);
                        SwingUtilities.invokeLater(() -> appendLog("OK: " + path.getFileName() + " → " + target));
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> appendLog("ERROR: " + ex.getMessage()));
                    }
                }, () -> SwingUtilities.invokeLater(() -> appendLog("No rule for " + path.getFileName())));
            });
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            appendLog("Watcher running (Dry-Run=" + cfg.isDryRun() + ")");
        } catch (Exception e) {
            appendLog("Failed to start: " + e.getMessage());
        }
    }

    private void stopWatching() {
        try {
            if (watcher != null) watcher.close();
        } catch (Exception ignored) {}
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
    }

    private void appendLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void saveConfig() {
        try {
            cfgIO.save(cfgFile, cfg);
        } catch (Exception e) {
            appendLog("Saving config failed: " + e.getMessage());
        }
    }
}
