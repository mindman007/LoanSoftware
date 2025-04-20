package com.mortgage.service;

import com.mortgage.database.DatabaseConnection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.PageSize;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ReportingService {
    
    public static class MortgageStatistics {
        public double totalLoanAmount;
        public int totalApplications;
        public Map<String, Integer> statusCounts;
        public double averageLoanAmount;
        public double averageInterestRate;
        public double totalGoldWeight;
        public Map<String, Double> monthlyLoanTotals;

        public MortgageStatistics() {
            statusCounts = new HashMap<>();
            monthlyLoanTotals = new TreeMap<>();
        }
    }

    public static MortgageStatistics getStatistics() {
        MortgageStatistics stats = new MortgageStatistics();

        String sql = """
            SELECT 
                COUNT(*) as total_applications,
                SUM(loan_amount) as total_loan_amount,
                AVG(loan_amount) as avg_loan_amount,
                AVG(interest_rate) as avg_interest_rate,
                SUM(weight) as total_gold_weight,
                status,
                DATE_TRUNC('month', application_date) as month
            FROM mortgages
            GROUP BY status, DATE_TRUNC('month', application_date)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.totalApplications += rs.getInt("total_applications");
                stats.totalLoanAmount += rs.getDouble("total_loan_amount");
                stats.averageLoanAmount = rs.getDouble("avg_loan_amount");
                stats.averageInterestRate = rs.getDouble("avg_interest_rate");
                stats.totalGoldWeight += rs.getDouble("total_gold_weight");
                
                String status = rs.getString("status");
                stats.statusCounts.merge(status, rs.getInt("total_applications"), Integer::sum);
                
                java.sql.Date month = rs.getDate("month");
                if (month != null) {
                    String monthKey = new SimpleDateFormat("MMM yyyy").format(month);
                    stats.monthlyLoanTotals.merge(monthKey, rs.getDouble("total_loan_amount"), Double::sum);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return stats;
    }

    public static void exportToExcel(String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mortgage Applications");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "ID", "Applicant Name", "Sex", "DOB", "Loan Amount", "Thing Name",
                "Interest Rate", "Application Date", "Weight", "Gold Rate",
                "Silver Rate", "Status", "Address"
            };
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Get data from database
            String sql = "SELECT * FROM mortgages ORDER BY application_date DESC";
            int rowNum = 1;
            
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getInt("mortgage_id"));
                    row.createCell(1).setCellValue(rs.getString("applicant_name"));
                    row.createCell(2).setCellValue(rs.getString("sex"));
                    row.createCell(3).setCellValue(rs.getDate("date_of_birth").toString());
                    row.createCell(4).setCellValue(rs.getDouble("loan_amount"));
                    row.createCell(5).setCellValue(rs.getString("thing_name"));
                    row.createCell(6).setCellValue(rs.getDouble("interest_rate"));
                    row.createCell(7).setCellValue(rs.getDate("application_date").toString());
                    row.createCell(8).setCellValue(rs.getDouble("weight"));
                    row.createCell(9).setCellValue(rs.getDouble("gold_rate"));
                    row.createCell(10).setCellValue(rs.getDouble("silver_rate"));
                    row.createCell(11).setCellValue(rs.getString("status"));
                    row.createCell(12).setCellValue(rs.getString("address"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }

    public static void exportToPDF(String filePath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Add title
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 
            18, 
            com.itextpdf.text.Font.BOLD
        );
        
        Paragraph title = new Paragraph("Mortgage Applications Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        // Create table
        PdfPTable table = new PdfPTable(8); // Using fewer columns for better fit
        table.setWidthPercentage(100);

        // Add header row
        String[] headers = {
            "ID", "Applicant", "Loan Amount", "Thing Name",
            "Interest", "Date", "Status", "Weight"
        };
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(cell);
        }

        // Add data
        String sql = "SELECT * FROM mortgages ORDER BY application_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("mortgage_id")));
                table.addCell(rs.getString("applicant_name"));
                table.addCell(String.format("₹%.2f", rs.getDouble("loan_amount")));
                table.addCell(rs.getString("thing_name"));
                table.addCell(String.format("%.2f%%", rs.getDouble("interest_rate")));
                table.addCell(rs.getDate("application_date").toString());
                table.addCell(rs.getString("status"));
                table.addCell(String.format("%.2f g", rs.getDouble("weight")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        document.add(table);
        document.close();
    }
} 