package com.mortgage.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class MortgageForm {
    private final JFrame parentFrame;
    private final String currentUser;

    public MortgageForm(JFrame parentFrame, String currentUser) {
        this.parentFrame = parentFrame;
        this.currentUser = currentUser;
    }

    public JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        addSectionHeader(formPanel, "Personal Information", gbc, row++);
        
        JTextField nameField = addFormField(formPanel, "Full Name:", row++, gbc);
        
        String[] sexOptions = {"Male", "Female", "Other"};
        JComboBox<String> sexComboBox = new JComboBox<>(sexOptions);
        addFormComponent(formPanel, "Sex:", sexComboBox, row++, gbc);
        
        // ... rest of the form components
        
        return formPanel;
    }

    // Helper methods for form components
    public void addSectionHeader(JPanel panel, String text, GridBagConstraints gbc, int row) {
    	JLabel header = new JLabel(text);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(header, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
    }

    public JTextField addFormField(JPanel panel, String labelText, int row, GridBagConstraints gbc) {

    	JLabel label = new JLabel(labelText);
        JTextField field = new JTextField(20);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);

        return field;
    }

    public void addFormComponent(JPanel panel, String labelText, JComponent component, int row, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }
}