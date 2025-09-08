package dev.mzcy.ui;

import dev.mzcy.model.Rule;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RuleTableModel extends AbstractTableModel {
    private final String[] cols = {"Name","Pattern","Typ","Ziel","Prio"};
    private final List<Rule> data = new ArrayList<>();

    public List<Rule> getRules() { return data; }

    public void addRule(Rule r) {
        data.add(r);
        fireTableRowsInserted(data.size()-1, data.size()-1);
    }

    public void removeAt(int row) {
        if (row>=0 && row<data.size()) {
            data.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override public Object getValueAt(int rowIndex, int columnIndex) {
        Rule r = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> r.getName();
            case 1 -> r.getPattern();
            case 2 -> r.getPatternType();
            case 3 -> r.getTargetDir()!=null ? r.getTargetDir().toString() : "";
            case 4 -> r.getPriority();
            default -> "";
        };
    }
}
