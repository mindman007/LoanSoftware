package com.mortgage.ui;

import com.mortgage.service.LicenseService;
import com.mortgage.service.LicenseValidator;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class LicenseManagementPanel extends JPanel {
    
    public LicenseManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Organization name field
        formPanel.add(new JLabel("Organization Name:"), gbc);
        gbc.gridx = 1;
        JTextField orgNameField = new JTextField(20);
        formPanel.add(orgNameField, gbc);

        // License key field
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("License Key:"), gbc);
        gbc.gridx = 1;
        JTextField licenseKeyField = new JTextField(20);
        formPanel.add(licenseKeyField, gbc);

        // Submit button
        gbc.gridx = 1;
        gbc.gridy = 2;
        JButton submitBtn = new JButton("Submit License");
        formPanel.add(submitBtn, gbc);

        // Add action listener
        submitBtn.addActionListener(e -> {
            String orgName = orgNameField.getText().trim();
            String licenseKey = licenseKeyField.getText().trim();
            
            if (orgName.isEmpty() || licenseKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter organization name and license key",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate the entered license key
            LocalDate[] licenseDates = LicenseValidator.decodeLicenseKey(licenseKey);
            if (licenseDates == null || !LicenseValidator.isLicenseValid(licenseKey, licenseDates[1])) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid or expired license key.",
                    "License Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add license entry to the database
            LocalDate startDate = licenseDates[0];
            LocalDate expiryDate = licenseDates[1];
            if (LicenseService.addLicenseEntry(licenseKey, startDate, expiryDate, true, orgName)) {
                JOptionPane.showMessageDialog(this,
                    "License added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                // Proceed to the next step or close the panel
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error saving license to the database",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        add(formPanel, BorderLayout.CENTER);
    }
}