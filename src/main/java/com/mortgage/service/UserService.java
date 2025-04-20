package com.mortgage.service;

// Add these new imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ListSelectionModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import com.mortgage.model.MortgageApplication;
import com.mortgage.model.UserRegistration;
import com.mortgage.ui.DashboardPanel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.mortgage.database.DatabaseConnection;
import com.mortgage.util.ButtonEditor;
import com.mortgage.util.ButtonRenderer;
import java.util.Date;

public class UserService {
    private final JFrame parentFrame;

    public UserService(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public boolean isAdmin(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT role FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && "ADMIN".equals(rs.getString("role"));
        }
    }

    public void showAllUsers() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("All Users");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Username", "Email", "Role", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "SELECT username, email, role FROM users WHERE role != 'ADMIN' ORDER BY username")) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("role"),
                    "View Mortgages"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "Error loading users: " + e.getMessage());
        }

        JTable table = new JTable(model);
        table.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        table.getColumn("Actions").setCellEditor(
            new ButtonEditor(new JCheckBox(), parentFrame, username -> {
                int row = table.getSelectedRow();
                String selectedUser = (String) table.getValueAt(row, 0);
                showApplicationStatus(selectedUser);  // Add this line to show the user's applications
            }));

        parentFrame.getContentPane().removeAll();
        parentFrame.add(mainPanel);
        mainPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backButton = new JButton("Back to Dashboard");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        parentFrame.revalidate();
        parentFrame.repaint();
    }

    public List<UserRegistration> getAllUserRegistrations() throws SQLException {
        List<UserRegistration> registrations = new ArrayList<>();
        String query = "SELECT username, email FROM users WHERE role != 'ADMIN'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                UserRegistration registration = new UserRegistration(
                    rs.getString("username"),
                    rs.getString("email")
                );
                registrations.add(registration);
            }
        }
        return registrations;
    }

    public List<MortgageApplication> getAllMortgageApplications() throws SQLException {
        List<MortgageApplication> applications = new ArrayList<>();
        String query = "SELECT * FROM mortgages ORDER BY submission_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                MortgageApplication app = new MortgageApplication();
                app.setId(rs.getInt("id"));
                app.setUserId(rs.getString("user_id"));
                app.setName(rs.getString("name"));
                app.setSex(rs.getString("sex"));
                app.setDateOfBirth(rs.getDate("date_of_birth"));
                app.setLoanAmount(rs.getDouble("loan_amount"));
                app.setThingName(rs.getString("thing_name"));
                app.setInterestRate(rs.getDouble("interest_rate"));
                app.setSubmissionDate(rs.getDate("submission_date"));
                app.setWeight(rs.getDouble("weight"));
                app.setGoldRate(rs.getDouble("gold_rate"));
                app.setSilverRate(rs.getDouble("silver_rate"));
                app.setAddress(rs.getString("address"));
                app.setStatus(rs.getString("status"));
                
                applications.add(app);
            }
        }
        return applications;
    }

    public MortgageApplication getMortgageApplication(int applicationId) throws SQLException {
        String query = "SELECT * FROM mortgages WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, applicationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                MortgageApplication app = new MortgageApplication();
                app.setId(rs.getInt("id"));
                app.setUserId(rs.getString("user_id"));
                app.setName(rs.getString("name"));
                app.setSex(rs.getString("sex"));
                app.setDateOfBirth(rs.getDate("date_of_birth"));
                app.setLoanAmount(rs.getDouble("loan_amount"));
                app.setThingName(rs.getString("thing_name"));
                app.setWeight(rs.getDouble("weight"));
                app.setGoldRate(rs.getDouble("gold_rate"));
                app.setSilverRate(rs.getDouble("silver_rate"));
                app.setAddress(rs.getString("address"));
                app.setStatus(rs.getString("status"));
                app.setSubmissionDate(rs.getDate("submission_date"));
                app.setInterestRate(rs.getDouble("interest_rate"));
                
                return app;
            }
            return null;
        }
    }

    public void updateMortgageStatus(int applicationId, String newStatus) throws SQLException {
        String query = "UPDATE mortgages SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, newStatus);
            stmt.setInt(2, applicationId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No mortgage application found with ID: " + applicationId);
            }
        }
    }

    private String generateUniqueMortgageNumber() {
        // Format: MG-YYYYMMDD-XXXX (where XXXX is a random number)
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = String.format("%04d", (int)(Math.random() * 10000));
        return "MG-" + datePart + "-" + randomPart;
    }

    public void submitMortgageApplication(MortgageApplication application) throws SQLException {
        String sql = "INSERT INTO mortgages (user_id, name, sex, date_of_birth, loan_amount, " +
                    "thing_name, interest_rate, submission_date, weight, gold_rate, silver_rate, address, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, application.getUserId());
            stmt.setString(2, application.getName());
            stmt.setString(3, application.getSex());
            stmt.setDate(4, new java.sql.Date(application.getDateOfBirth().getTime()));
            stmt.setDouble(5, application.getLoanAmount());
            stmt.setString(6, application.getThingName());
            stmt.setDouble(7, application.getInterestRate());
            stmt.setDate(8, new java.sql.Date(application.getSubmissionDate().getTime()));
            stmt.setDouble(9, application.getWeight());
            stmt.setDouble(10, application.getGoldRate());
            stmt.setDouble(11, application.getSilverRate());
            stmt.setString(12, application.getAddress());
            stmt.setString(13, application.getStatus());
            
            stmt.executeUpdate();
        }
    }

    public void showApplicationStatus(String currentUser) {
        JPanel applicationStatusPanel = new JPanel(new BorderLayout(10, 10));
        applicationStatusPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        // Add title and search panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Mortgage Applications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(titleLabel, BorderLayout.NORTH);
    
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Name", "Date", "Address", "Advanced"});
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear Search");
        
        // Add advanced search panel (initially invisible)
        JPanel advancedSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nameField = new JTextField(15);
        JTextField dateField = new JTextField(15);
        JTextField addressField = new JTextField(15);
        
        advancedSearchPanel.add(new JLabel("Name:"));
        advancedSearchPanel.add(nameField);
        advancedSearchPanel.add(new JLabel("Date:"));
        advancedSearchPanel.add(dateField);
        advancedSearchPanel.add(new JLabel("Address:"));
        advancedSearchPanel.add(addressField);
        advancedSearchPanel.setVisible(false);
        
        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchType);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        JPanel searchContainer = new JPanel(new BorderLayout());
        searchContainer.add(searchPanel, BorderLayout.NORTH);
        searchContainer.add(advancedSearchPanel, BorderLayout.CENTER);
        topPanel.add(searchContainer, BorderLayout.SOUTH);

        // Add the topPanel to applicationStatusPanel
        applicationStatusPanel.add(topPanel, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Application ID", "Name", "Loan Amount", "Thing Name", "Status", "Submission Date", "Address"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);

        // Load initial data
        loadApplicationData(model, currentUser, null, null);  // Add this line to load initial data

        // Modify search button action
        searchButton.addActionListener(e -> {
            model.setRowCount(0); // Clear existing data
            if ("Advanced".equals(searchType.getSelectedItem())) {
                loadApplicationData(model, currentUser, "Advanced",
                    nameField.getText().trim(),
                    dateField.getText().trim(),
                    addressField.getText().trim());
            } else {
                loadApplicationData(model, currentUser,
                    (String) searchType.getSelectedItem(),
                    searchField.getText().trim());
            }
        });

        // Modify clear button action
        clearButton.addActionListener(e -> {
            searchField.setText("");
            nameField.setText("");
            dateField.setText("");
            addressField.setText("");
            model.setRowCount(0);
            loadApplicationData(model, currentUser, null, null);
        });

        // Add table and other components
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int applicationId = (Integer) table.getValueAt(row, 0);
                        showApplicationDetails(applicationId);
                    }
                }
            }
        });
    
        applicationStatusPanel.add(new JScrollPane(table), BorderLayout.CENTER);
    
        // Add back button
        JButton backButton = new JButton("Back to Dashboard");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);
        applicationStatusPanel.add(bottomPanel, BorderLayout.SOUTH);
    
        backButton.addActionListener(e -> {
            DashboardPanel dashboard = new DashboardPanel(parentFrame, currentUser, this);
            parentFrame.getContentPane().removeAll();
            parentFrame.add(dashboard.createDashboard());
            parentFrame.revalidate();
            parentFrame.repaint();
        });

        parentFrame.getContentPane().removeAll();
        parentFrame.add(applicationStatusPanel);
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    // Add this utility method for interest calculation
    private double calculateInterestAmount(double principal, double monthlyRate, Date loanDate) {
        // Convert monthly rate to daily rate
        double dailyRate = monthlyRate / 30.0;
        
        // Calculate days between loan date and current date
        long diffInMillies = System.currentTimeMillis() - loanDate.getTime();
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        
        // Calculate interest amount
        return (principal * dailyRate * diffInDays) / 100.0;
    }

    private void showApplicationDetails(int applicationId) {
        try {
            MortgageApplication app = getMortgageApplication(applicationId);
            if (app != null) {
                // Calculate interest amount
                double interestAmount = calculateInterestAmount(
                    app.getLoanAmount(),
                    app.getInterestRate(),
                    app.getSubmissionDate()
                );

                JDialog detailsDialog = new JDialog(parentFrame, "Application Details", true);
                detailsDialog.setLayout(new BorderLayout());
                detailsDialog.setSize(400, 500);
                detailsDialog.setLocationRelativeTo(parentFrame);
    
                JPanel detailsPanel = new JPanel(new GridBagLayout());
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(5, 5, 5, 5);
    
                // Add all details
                addDetailField(detailsPanel, gbc, "Application ID:", String.valueOf(app.getId()));
                addDetailField(detailsPanel, gbc, "Name:", app.getName());
                addDetailField(detailsPanel, gbc, "Sex:", app.getSex());
                addDetailField(detailsPanel, gbc, "Date of Birth:", app.getDateOfBirth().toString());
                addDetailField(detailsPanel, gbc, "Loan Amount:", String.format("$%.2f", app.getLoanAmount()));
                addDetailField(detailsPanel, gbc, "Thing Name:", app.getThingName());
                addDetailField(detailsPanel, gbc, "Interest Rate:", app.getInterestRate() + "%");
                addDetailField(detailsPanel, gbc, "Weight:", app.getWeight() + " g");
                addDetailField(detailsPanel, gbc, "Gold Rate:", String.format("$%.2f", app.getGoldRate()));
                addDetailField(detailsPanel, gbc, "Silver Rate:", String.format("$%.2f", app.getSilverRate()));
                addDetailField(detailsPanel, gbc, "Address:", app.getAddress());
                addDetailField(detailsPanel, gbc, "Status:", app.getStatus());
                addDetailField(detailsPanel, gbc, "Submission Date:", app.getSubmissionDate().toString());

                // Add prepayment panel
                JPanel prepaymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JTextField prepaymentField = new JTextField(10);
                JButton updateButton = new JButton("Make Prepayment");
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
                        updateLoanAmount(app.getId(), newLoanAmount);

                        JOptionPane.showMessageDialog(detailsDialog,
                            "Prepayment of ₹" + String.format("%.2f", prepayment) + " processed successfully!\n" +
                            "New loan amount: ₹" + String.format("%.2f", newLoanAmount),
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
                            "Error processing prepayment: " + ex.getMessage(),
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
            JOptionPane.showMessageDialog(parentFrame,
                "Error loading application details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addDetailField(JPanel panel, GridBagConstraints gbc, String label, String value) {
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
        gbc.gridy++;
    }

    public double getUserTotalInterest(String userId) throws SQLException {
        String sql = "SELECT loan_amount, interest_rate, submission_date FROM mortgages WHERE user_id = ?";
        double totalInterest = 0.0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double interestAmount = calculateInterestAmount(
                        rs.getDouble("loan_amount"),
                        rs.getDouble("interest_rate"),
                        rs.getDate("submission_date")
                    );
                    totalInterest += interestAmount;
                }
            }
            return totalInterest;
        }
    }

    public int getOldApplicationsCount(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mortgages WHERE user_id = ? AND submission_date <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Calculate date 1 year ago
            java.sql.Date oneYearAgo = new java.sql.Date(System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000);
            
            pstmt.setString(1, userId);
            pstmt.setDate(2, oneYearAgo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    // Remove these methods completely
    // public int getUserPendingLoansCount(String userId) throws SQLException { ... }
    // public int getUserApprovedLoansCount(String userId) throws SQLException { ... }

    // Keep the new methods we added
    public double getUserTotalLoanAmount(String userId) throws SQLException {
        String sql = "SELECT loan_amount, interest_rate, submission_date FROM mortgages WHERE user_id = ?";
        double totalAmount = 0.0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double loanAmount = rs.getDouble("loan_amount");
                    double interestAmount = calculateInterestAmount(
                        loanAmount,
                        rs.getDouble("interest_rate"),
                        rs.getDate("submission_date")
                    );
                    totalAmount += (loanAmount + interestAmount);
                }
            }
            return totalAmount;
        }
    }

    public int getUserTotalLoans(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mortgages WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

// Add this method after showApplicationStatus method
    // Update the existing method signature
    private void loadApplicationData(DefaultTableModel model, String currentUser, String searchType, String searchText) {
        loadApplicationData(model, currentUser, searchType, searchText, null, null);
    }

    // Add the new overloaded method for advanced search
    private void loadApplicationData(DefaultTableModel model, String currentUser, String searchType,
                                   String nameSearch, String dateSearch, String addressSearch) {
        try {
            StringBuilder sql = new StringBuilder(
                "SELECT * FROM mortgages WHERE user_id = ? ");
            
            if ("Advanced".equals(searchType)) {
                if (nameSearch != null && !nameSearch.isEmpty()) {
                    sql.append("AND LOWER(name) LIKE LOWER(?) ");
                }
                if (dateSearch != null && !dateSearch.isEmpty()) {
                    sql.append("AND CAST(submission_date AS VARCHAR) LIKE ? ");
                }
                if (addressSearch != null && !addressSearch.isEmpty()) {
                    sql.append("AND LOWER(address) LIKE LOWER(?) ");
                }
            } else if (searchType != null && !searchType.isEmpty() && nameSearch != null && !nameSearch.isEmpty()) {
                switch (searchType) {
                    case "Name":
                        sql.append("AND LOWER(name) LIKE LOWER(?) ");
                        break;
                    case "Date":
                        sql.append("AND CAST(submission_date AS VARCHAR) LIKE ? ");
                        break;
                    case "Address":
                        sql.append("AND LOWER(address) LIKE LOWER(?) ");
                        break;
                }
            }
            sql.append("ORDER BY submission_date DESC");

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                
                int paramIndex = 1;
                stmt.setString(paramIndex++, currentUser);

                if ("Advanced".equals(searchType)) {
                    if (nameSearch != null && !nameSearch.isEmpty()) {
                        stmt.setString(paramIndex++, "%" + nameSearch + "%");
                    }
                    if (dateSearch != null && !dateSearch.isEmpty()) {
                        stmt.setString(paramIndex++, "%" + dateSearch + "%");
                    }
                    if (addressSearch != null && !addressSearch.isEmpty()) {
                        stmt.setString(paramIndex++, "%" + addressSearch + "%");
                    }
                } else if (searchType != null && !searchType.isEmpty() && nameSearch != null && !nameSearch.isEmpty()) {
                    stmt.setString(paramIndex++, "%" + nameSearch + "%");
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    double loanAmount = rs.getDouble("loan_amount");
                    double interestAmount = calculateInterestAmount(
                        loanAmount,
                        rs.getDouble("interest_rate"),
                        rs.getDate("submission_date")
                    );

                    model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        String.format("₹%.2f (Int: ₹%.2f)", loanAmount, interestAmount),
                        rs.getString("thing_name"),
                        rs.getString("status"),
                        rs.getDate("submission_date"),
                        rs.getString("address")
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentFrame,
                "Error loading applications: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateLoanAmount(int applicationId, double newLoanAmount) throws SQLException {
        String sql = "UPDATE mortgages SET loan_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newLoanAmount);
            pstmt.setInt(2, applicationId);
            pstmt.executeUpdate();
        }
    }
}