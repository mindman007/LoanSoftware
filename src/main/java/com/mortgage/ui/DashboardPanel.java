package com.mortgage.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.sql.SQLException;

import com.itextpdf.text.DocumentException;
import com.mortgage.Main;
import com.mortgage.model.MortgageApplication;
import com.mortgage.model.UserRegistration;
import com.mortgage.service.ReportingService;
import com.mortgage.service.UserService;

import org.jdatepicker.impl.*;
import org.jdatepicker.util.JDatePickerUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class DashboardPanel {
    private JFrame mainFrame;
    private String currentUser;
    private UserService userService;
    private JPanel mainPanel;

    public DashboardPanel(JFrame frame, String username, UserService userService) {
        this.mainFrame = frame;
        this.currentUser = username;
        this.userService = userService;
    }

    public JPanel createDashboard() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Create top panel with user info
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("Welcome, " + currentUser);
        userLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(userLabel, BorderLayout.WEST);

        // Add logout button to top panel
        JButton logoutBtn = new JButton("Logout");
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.add(logoutBtn);
        topPanel.add(navPanel, BorderLayout.EAST);

        // Logout button action
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                mainFrame.dispose();
                SwingUtilities.invokeLater(() -> new Main());
            }
        });

        // Create left navigation panel
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        leftPanel.setBackground(new Color(240, 240, 240));

        // Create navigation buttons
        JButton newRegistrationBtn = createMenuButton("New Registration");
        JButton viewRegistrationsBtn = createMenuButton("View Registrations");

        // Add buttons to left panel
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(newRegistrationBtn);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(viewRegistrationsBtn);
        leftPanel.add(Box.createVerticalGlue());

        // Add button actions
        newRegistrationBtn.addActionListener(e -> showMortgageApplication());
        viewRegistrationsBtn.addActionListener(e -> userService.showApplicationStatus(currentUser));

        // Create right panel for consolidated report
        JPanel rightPanel = createConsolidatedReportPanel();
        rightPanel.setPreferredSize(new Dimension(250, 0));

        // Create main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel welcomeMsg = new JLabel("Welcome to Mortgage Management System");
        welcomeMsg.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeMsg.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(welcomeMsg, BorderLayout.CENTER);

        // Add all panels to main container
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        return mainPanel;
    }

    private JPanel createConsolidatedReportPanel() {
        JPanel reportPanel = new JPanel();
        reportPanel.setLayout(new BoxLayout(reportPanel, BoxLayout.Y_AXIS));
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        reportPanel.setBackground(new Color(245, 245, 245));

        // Title
        JLabel titleLabel = new JLabel("My Loan Summary");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        reportPanel.add(titleLabel);
        reportPanel.add(Box.createVerticalStrut(20));

        try {
            // Get user-specific consolidated data from UserService
            double totalLoanAmount = userService.getUserTotalLoanAmount(currentUser);
            int totalLoans = userService.getUserTotalLoans(currentUser);
            double totalInterest = userService.getUserTotalInterest(currentUser);
            int oldApplications = userService.getOldApplicationsCount(currentUser);
            
            // Create info panels
            addInfoPanel(reportPanel, "Total Loan Amount", String.format("₹%.2f", totalLoanAmount));
            addInfoPanel(reportPanel, "Total Applications", String.valueOf(totalLoans));
            addInfoPanel(reportPanel, "Total Interest Till Now", String.format("₹%.2f", totalInterest));
            addInfoPanel(reportPanel, "Applications > 1 Year", String.valueOf(oldApplications));
           
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame,
                "Error loading user report: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        return reportPanel;
    }

    private void addInfoPanel(JPanel container, String label, String value) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        infoPanel.setMaximumSize(new Dimension(200, 80));
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(new Color(41, 128, 185));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(titleLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(valueLabel);

        container.add(infoPanel);
        container.add(Box.createVerticalStrut(15));
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        return button;
    }

    private void showUserRegistrations() {
        JPanel registrationsPanel = new JPanel(new BorderLayout(10, 10));
        registrationsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add title
        JLabel titleLabel = new JLabel("User Registrations");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        registrationsPanel.add(titleLabel, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Username", "Email", "Role"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        
        // Fetch and display user registrations
        try {
            List<UserRegistration> registrations = userService.getAllUserRegistrations();
            for (UserRegistration reg : registrations) {
                model.addRow(new Object[]{
                    reg.getUsername(),
                    reg.getEmail(),
                    "USER"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Error loading user registrations: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        // Add table to panel
        registrationsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add back button
        JButton backButton = new JButton("Back to Dashboard");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);
        registrationsPanel.add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createDashboard());
            mainPanel.revalidate();
            mainPanel.repaint();
        });

        mainPanel.removeAll();
        mainPanel.add(registrationsPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showMortgageManagement() {
        JPanel mortgagePanel = new JPanel(new BorderLayout());
        
        // Add a top panel for the back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("Back to Dashboard");
        topPanel.add(backButton);
        mortgagePanel.add(topPanel, BorderLayout.NORTH);
        
        // Create table for mortgage applications
        String[] columns = {"Application ID", "User", "Name", "Loan Amount", "Status", "Submission Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Add controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        JButton viewDetailsBtn = new JButton("View Details");
        JButton exportExcelBtn = new JButton("Export to Excel");
        JButton exportPdfBtn = new JButton("Export to PDF");
        
        controlsPanel.add(approveBtn);
        controlsPanel.add(rejectBtn);
        controlsPanel.add(viewDetailsBtn);
        controlsPanel.add(exportExcelBtn);
        controlsPanel.add(exportPdfBtn);
        
        // Load mortgage applications
        try {
            List<MortgageApplication> applications = userService.getAllMortgageApplications();
            for (MortgageApplication app : applications) {
                model.addRow(new Object[]{
                    app.getId(),
                    app.getUserId(),
                    app.getName(),
                    app.getLoanAmount(),
                    app.getStatus(),
                    app.getSubmissionDate()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Error loading mortgage applications: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        // Button actions
        approveBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int applicationId = (int) table.getValueAt(selectedRow, 0);
                try {
                    userService.updateMortgageStatus(applicationId, "APPROVED");
                    model.setValueAt("APPROVED", selectedRow, 4);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Error updating status: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Please select an application");
            }
        });
        
        rejectBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int applicationId = (int) table.getValueAt(selectedRow, 0);
                try {
                    userService.updateMortgageStatus(applicationId, "REJECTED");
                    model.setValueAt("REJECTED", selectedRow, 4);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Error updating status: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Please select an application");
            }
        });
        
        viewDetailsBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                int applicationId = (int) table.getValueAt(selectedRow, 0);
                showApplicationDetails(applicationId);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Please select an application");
            }
        });
        
        // Add back button action listener
        backButton.addActionListener(e -> {
            mainPanel.removeAll();
            mainPanel.add(createDashboard());
            mainPanel.revalidate();
            mainPanel.repaint();
        });
        
        mortgagePanel.add(scrollPane, BorderLayout.CENTER);
        mortgagePanel.add(controlsPanel, BorderLayout.SOUTH);
        
        mainPanel.removeAll();
        mainPanel.add(mortgagePanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
        
     // Add export buttons actions
        exportExcelBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel Report");
            fileChooser.setSelectedFile(new File("mortgage_report.xlsx"));
            
            if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    ReportingService.exportToExcel(fileChooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(mainFrame, "Report exported successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        exportPdfBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF Report");
            fileChooser.setSelectedFile(new File("mortgage_report.pdf"));
            
            if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    ReportingService.exportToPDF(fileChooser.getSelectedFile().getAbsolutePath());
                    JOptionPane.showMessageDialog(mainFrame, "Report exported successfully!");
                } catch (IOException | DocumentException ex) {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void showApplicationDetails(int applicationId) {
        try {
            MortgageApplication app = userService.getMortgageApplication(applicationId);
            if (app != null) {
                JDialog detailsDialog = new JDialog(mainFrame, "Application Details", true);
                detailsDialog.setLayout(new BorderLayout());
                detailsDialog.setSize(500, 400);
                detailsDialog.setLocationRelativeTo(mainFrame);

                JPanel detailsPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(5, 5, 5, 5);

                // Add all application details
                addDetailField(detailsPanel, gbc, "Application ID:", String.valueOf(app.getId()));
                addDetailField(detailsPanel, gbc, "User:", app.getUserId());
                addDetailField(detailsPanel, gbc, "Name:", app.getName());
                addDetailField(detailsPanel, gbc, "Sex:", app.getSex());
                addDetailField(detailsPanel, gbc, "Date of Birth:", app.getDateOfBirth().toString());
                addDetailField(detailsPanel, gbc, "Loan Amount:", String.format("₹%.2f", app.getLoanAmount()));
                addDetailField(detailsPanel, gbc, "Thing Name:", app.getThingName());
                addDetailField(detailsPanel, gbc, "Interest Rate:", app.getInterestRate() + "%");
                addDetailField(detailsPanel, gbc, "Weight:", app.getWeight() + " g");
                addDetailField(detailsPanel, gbc, "Address:", app.getAddress());
                addDetailField(detailsPanel, gbc, "Status:", app.getStatus());
                addDetailField(detailsPanel, gbc, "Submission Date:", app.getSubmissionDate().toString());

                // Add prepayment panel
                JPanel prepaymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField prepaymentField = new JTextField(10);
                JButton updateButton = new JButton("Update Loan Amount");
                prepaymentPanel.add(new JLabel("Prepayment Amount (₹):"));
                prepaymentPanel.add(prepaymentField);
                prepaymentPanel.add(updateButton);

                updateButton.addActionListener(e -> {
                    try {
                        double prepayment = Double.parseDouble(prepaymentField.getText());
                        if (prepayment <= 0) {
                            throw new IllegalArgumentException("Prepayment amount must be positive");
                        }
                        if (prepayment >= app.getLoanAmount()) {
                            throw new IllegalArgumentException("Prepayment cannot exceed loan amount");
                        }
                        
                        double newLoanAmount = app.getLoanAmount() - prepayment;
                        userService.updateLoanAmount(app.getId(), newLoanAmount);
                        
                        JOptionPane.showMessageDialog(detailsDialog,
                            "Loan amount updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                        detailsDialog.dispose();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(detailsDialog,
                            "Please enter a valid amount",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    } catch (IllegalArgumentException ex) {
                        JOptionPane.showMessageDialog(detailsDialog,
                            ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(detailsDialog,
                            "Error updating loan amount: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });

                JPanel bottomPanel = new JPanel(new BorderLayout());
                bottomPanel.add(prepaymentPanel, BorderLayout.CENTER);
                
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> detailsDialog.dispose());
                bottomPanel.add(closeButton, BorderLayout.SOUTH);

                detailsDialog.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);
                detailsDialog.add(bottomPanel, BorderLayout.SOUTH);
                detailsDialog.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame,
                "Error loading application details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }


    private JTextField addFormField(JPanel panel, GridBagConstraints gbc, String label) {
        panel.add(new JLabel(label), gbc);
        JTextField field = new JTextField(20);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        return field;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JComponent field) {
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
    }

    private void addDetailField(JPanel panel, GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
        gbc.gridy++;
    }

    private void showApplicationStatus() {
        JPanel applicationStatusPanel = new JPanel(new BorderLayout());
        // Create table for application status
        String[] columns = {"Application ID", "User", "Name", "Loan Amount", "Status", "Submission Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        mainPanel.removeAll();
        mainPanel.add(applicationStatusPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private JDatePickerImpl createDatePicker() {
        UtilDateModel model = new UtilDateModel();
        // Set today's date as default
        Calendar today = Calendar.getInstance();
        model.setDate(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        );
        model.setSelected(true);
        
        Properties properties = new Properties();
        properties.put("text.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        return new JDatePickerImpl(datePanel, new JFormattedTextField.AbstractFormatter() {
            private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

            @Override
            public Object stringToValue(String text) throws ParseException {
                return dateFormatter.parse(text);
            }

            @Override
            public String valueToString(Object value) throws ParseException {
                if (value != null) {
                    if (value instanceof Calendar) {
                        return dateFormatter.format(((Calendar) value).getTime());
                    } else if (value instanceof Date) {
                        return dateFormatter.format((Date) value);
                    }
                }
                return "";
            }
        });
    }

    // Remove the separate DateLabelFormatter class as we're using anonymous class above
    private void showMortgageApplication() {
            JDialog applicationDialog = new JDialog(mainFrame, "Mortgage Application", true);
            applicationDialog.setLayout(new BorderLayout());
            applicationDialog.setSize(600, 500);
            applicationDialog.setLocationRelativeTo(mainFrame);
    
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);
    
            // Form fields
            JTextField nameField = addFormField(formPanel, gbc, "Full Name:");
            JComboBox<String> sexCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
            addFormField(formPanel, gbc, "Sex:", sexCombo);
            
            JDatePickerImpl dobPicker = createDatePicker();
            addFormField(formPanel, gbc, "Date of Birth:", dobPicker);
            
            // In showMortgageApplication method, update the field labels
            JTextField loanAmountField = addFormField(formPanel, gbc, "Loan Amount (₹):");
            JTextField thingNameField = addFormField(formPanel, gbc, "Thing Name:");
            JTextField interestRateField = addFormField(formPanel, gbc, "Interest Rate (%):");
            JTextField weightField = addFormField(formPanel, gbc, "Weight (g):");
            JTextField goldRateField = addFormField(formPanel, gbc, "Gold Rate (₹):");
            JTextField silverRateField = addFormField(formPanel, gbc, "Silver Rate (₹):");
            JTextArea addressArea = new JTextArea(3, 20);
            addFormField(formPanel, gbc, "Address:", new JScrollPane(addressArea));
    
            // Buttons panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton submitButton = new JButton("Submit");
            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(cancelButton);
            buttonPanel.add(submitButton);
    
            // Submit action
            submitButton.addActionListener(e -> {
                try {
                    if (nameField.getText().isEmpty() || dobPicker.getModel().getValue() == null || 
                        loanAmountField.getText().isEmpty() || addressArea.getText().isEmpty()) {
                        throw new IllegalArgumentException("Please fill in all required fields");
                    }
    
                    MortgageApplication application = new MortgageApplication();
                    application.setUserId(currentUser);
                    application.setName(nameField.getText());
                    application.setSex((String) sexCombo.getSelectedItem());
                    
                    Date dobDate = (Date) dobPicker.getModel().getValue();
                    java.sql.Date dob = new java.sql.Date(dobDate.getTime());
                    application.setDateOfBirth(dob);
    
                    application.setLoanAmount(Double.parseDouble(loanAmountField.getText()));
                    application.setThingName(thingNameField.getText());
                    application.setInterestRate(Double.parseDouble(interestRateField.getText()));
                    application.setWeight(Double.parseDouble(weightField.getText()));
                    application.setGoldRate(Double.parseDouble(goldRateField.getText()));
                    application.setSilverRate(Double.parseDouble(silverRateField.getText()));
                    application.setAddress(addressArea.getText());
                    application.setStatus("PENDING");
                    application.setSubmissionDate(new java.sql.Date(System.currentTimeMillis()));
    
                    userService.submitMortgageApplication(application);
                    JOptionPane.showMessageDialog(applicationDialog, 
                        "Application submitted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    applicationDialog.dispose();
    
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(applicationDialog,
                        "Please enter valid numbers for amount and rates",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(applicationDialog,
                        ex.getMessage(),
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(applicationDialog,
                        "Error submitting application: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
    
            cancelButton.addActionListener(e -> applicationDialog.dispose());
    
            applicationDialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
            applicationDialog.add(buttonPanel, BorderLayout.SOUTH);
            applicationDialog.setVisible(true);
        }
}