package com.mortgage.util;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ButtonEditor extends DefaultCellEditor {
    protected JButton button;
    private String label;
    private boolean isPushed;
    private final Component parent;
    private final Consumer<String> action;
    private JTable table; // Add table field

    public ButtonEditor(JCheckBox checkBox, Component parent, Consumer<String> action) {
        super(checkBox);
        this.parent = parent;
        this.action = action;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.table = table; // Store table reference
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed && table != null) {
            String username = (String) table.getValueAt(table.getSelectedRow(), 0);
            action.accept(username);
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}