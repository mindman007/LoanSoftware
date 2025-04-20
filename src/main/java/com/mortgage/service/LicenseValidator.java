package com.mortgage.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class LicenseValidator {

    public static boolean isLicenseValid(String licenseKey, LocalDate expirationDate) {
        LocalDate currentDate = LocalDate.now();
        return !currentDate.isAfter(expirationDate);
    }

    public static LocalDate[] decodeLicenseKey(String licenseKey) {
        try {
            // Decode the license key (assuming it's Base64 encoded)
            String decodedData = new String(Base64.getDecoder().decode(licenseKey));
            String[] parts = decodedData.split("-");
            LocalDate startDate = LocalDate.parse(parts[0], DateTimeFormatter.ISO_DATE);
            LocalDate expirationDate = LocalDate.parse(parts[1], DateTimeFormatter.ISO_DATE);
            return new LocalDate[]{startDate, expirationDate};
        } catch (Exception e) {
            System.out.println("Failed to decode license key.");
            return null;
        }
    }
}