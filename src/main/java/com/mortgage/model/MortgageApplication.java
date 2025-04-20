package com.mortgage.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public class MortgageApplication {
    private int id;
    private String userId;
    private String name;
    private String sex;
    private Date dateOfBirth;
    private double loanAmount;
    private String thingName;
    private double weight;
    private double goldRate;
    private double silverRate;
    private String address;
    private String status;
    private Date submissionDate;
    private double interestRate;
    private int agentId;
    private LocalDate startDate;
    private LocalDate applicationDate;
    private int differenceDays;
    private BigDecimal interestAmount;
    private String uniqueNumber;
    private LocalDate createdAt;

    public MortgageApplication() {}

    // Getters and Setters
    // Add these getter and setter methods
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    
    public double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(double loanAmount) { this.loanAmount = loanAmount; }
    
    public String getThingName() { return thingName; }
    public void setThingName(String thingName) { this.thingName = thingName; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public double getGoldRate() { return goldRate; }
    public void setGoldRate(double goldRate) { this.goldRate = goldRate; }
    
    public double getSilverRate() { return silverRate; }
    public void setSilverRate(double silverRate) { this.silverRate = silverRate; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Date getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(Date submissionDate) { this.submissionDate = submissionDate; }
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public int getDifferenceDays() {
        return differenceDays;
    }

    public void setDifferenceDays(int differenceDays) {
        this.differenceDays = differenceDays;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public String getUniqueNumber() {
        return uniqueNumber;
    }

    public void setUniqueNumber(String uniqueNumber) {
        this.uniqueNumber = uniqueNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}