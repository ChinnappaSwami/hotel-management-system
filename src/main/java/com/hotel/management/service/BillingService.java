package com.hotel.management.service;

public final class BillingService {

    private BillingService() {
    }

    public static double calculateTotal(double pricePerDay, int numberOfDays, boolean includeTax, double taxPercent) {
        double subtotal = pricePerDay * numberOfDays;
        if (!includeTax) {
            return subtotal;
        }
        return subtotal + (subtotal * taxPercent / 100.0);
    }
}
