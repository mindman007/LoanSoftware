package com.mortgage.controller;

import com.mortgage.service.MortgageService;

public class AdminController {
    private MortgageService mortgageService;

    public AdminController() {
        this.mortgageService = new MortgageService();
    }

    public void modifyMortgageEntry(int mortgageId, String newDetails) {
        boolean success = mortgageService.updateMortgageEntry(mortgageId, newDetails);
        if (success) {
            System.out.println("Mortgage entry updated successfully.");
        } else {
            System.out.println("Failed to update mortgage entry.");
        }
    }
}