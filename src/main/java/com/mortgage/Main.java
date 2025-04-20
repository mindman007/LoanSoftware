package com.mortgage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.time.LocalDate;

import com.mortgage.database.DatabaseConnection;
import com.mortgage.controller.LoginController;
import com.mortgage.service.UserService;
import com.mortgage.service.LicenseService;
import com.mortgage.service.LicenseValidator;
import com.mortgage.ui.DashboardPanel;
import com.mortgage.ui.LicenseManagementPanel;
import com.mortgage.ui.MortgageUI;


public class Main implements LoginController.LoginCallback {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;
    private LoginController loginController;
    private String currentUser;
    private UserService userService;

    public Main() {
        SwingUtilities.invokeLater(() -> {
            initializeFrame();
            initializeServices();
            setupUI();  // Setup UI first
            frame.setVisible(true);  // Ensure frame is visible
        });
    }

    private void initializeFrame() {
        frame = new JFrame("Mortgage Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(false);  // Keep invisible until fully initialized
    }

    // Update main method
    public static void main(String[] args) {
        // Create license table if not exists
        //LicenseService.createLicenseTable();
        
        new Main();
    }

    private void initializeServices() {
        try {
            // Initialize database and create required tables
            DatabaseConnection.initializeDatabase();
            DatabaseConnection.createMortgagesTable();
            
            // Initialize services
            userService = new UserService(frame);
            
        } catch (SQLException e) {
            e.printStackTrace();
            String errorMessage = "Database initialization failed: " + e.getMessage() + 
                                "\nPlease ensure PostgreSQL is running and the database exists.";
            JOptionPane.showMessageDialog(null,
                errorMessage,
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Application initialization failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    @Override
    public void onLoginSuccess(String username) {
        this.currentUser = username;
        
        // Check if there is a valid license
//        if (!LicenseService.hasValidLicense()) {
//            SwingUtilities.invokeLater(() -> {
//                frame.getContentPane().removeAll();
//                LicenseManagementPanel licensePanel = new LicenseManagementPanel();
//                frame.getContentPane().add(licensePanel, BorderLayout.CENTER);
//                frame.setTitle("License Management - No Valid License");
//                frame.validate();
//                frame.repaint();
//            });
//            return;
//        }

        // Proceed to the dashboard if a valid license is present
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            DashboardPanel dashboard = new DashboardPanel(frame, currentUser, userService);
            JPanel dashboardPanel = dashboard.createDashboard();
            frame.getContentPane().add(dashboardPanel);
            frame.setTitle("Mortgage Management System - " + currentUser);
            frame.validate();
            frame.repaint();
        });
    }

    private void setupUI() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Mortgage Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        // User Type Selection
        gbc.gridy = 1;
        loginPanel.add(new JLabel("Login As:"), gbc);
        String[] userTypes = {"Select Type", "Admin", "User"};
        JComboBox<String> userTypeCombo = new JComboBox<>(userTypes);
        gbc.gridy = 2;
        loginPanel.add(userTypeCombo, gbc);

        // Username field
        gbc.gridwidth = 1;
        gbc.gridy = 3;
        loginPanel.add(new JLabel("Username:"), gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        // Password field
        gbc.gridx = 0;
        gbc.gridy = 4;
        loginPanel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        loginPanel.add(errorLabel, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        gbc.gridy = 6;
        loginPanel.add(buttonPanel, gbc);

        loginController = new LoginController(this);

        // Login button action
        loginButton.addActionListener(e -> {
            String selectedType = (String) userTypeCombo.getSelectedItem();
            if ("Select Type".equals(selectedType)) {
                JOptionPane.showMessageDialog(frame, "Please select a user type");
                return;
            }
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            loginController.handleLogin(username, password, selectedType);
        });

        // Register button action
        registerButton.addActionListener(e -> {
            String selectedType = (String) userTypeCombo.getSelectedItem();
            if (!"User".equals(selectedType)) {
                JOptionPane.showMessageDialog(frame, "Registration is only available for Users");
                return;
            }
            showRegistrationForm();
        });

        // Enter key support
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loginButton.doClick();
                }
            }
        });

        mainPanel.add(loginPanel);
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private void showRegistrationForm() {
        JDialog registerDialog = new JDialog(frame, "User Registration", true);
        registerDialog.setLayout(new BorderLayout());
        registerDialog.setSize(400, 300);
        registerDialog.setLocationRelativeTo(frame);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        JTextField emailField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Password:"), gbc);
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        formPanel.add(confirmPasswordField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Please fill in all fields");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerDialog, "Passwords do not match");
                return;
            }

            loginController.registerUser(username, password, email);
            registerDialog.dispose();
        });

        cancelButton.addActionListener(e -> registerDialog.dispose());

        registerDialog.add(formPanel, BorderLayout.CENTER);
        registerDialog.add(buttonPanel, BorderLayout.SOUTH);
        registerDialog.setVisible(true);
    }
}