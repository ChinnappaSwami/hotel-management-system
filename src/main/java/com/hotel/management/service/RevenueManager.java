package com.hotel.management.service;

import com.hotel.management.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class RevenueManager {

    private static RevenueManager instance;

    public static synchronized RevenueManager getInstance() {
        if (instance == null) {
            instance = new RevenueManager();
        }
        return instance;
    }

    private RevenueManager() {
    }

    public Map<String, Double> getRevenueByRoomType() throws Exception {
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        revenueMap.put("SINGLE", 0.0);
        revenueMap.put("DOUBLE", 0.0);
        revenueMap.put("SUITE",  0.0);

        String sql = "SELECT r.room_type, SUM(r.price_per_day * b.number_of_days) AS total_revenue "
                + "FROM bookings b "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "GROUP BY r.room_type";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String type    = rs.getString("room_type").toUpperCase();
                double revenue = rs.getDouble("total_revenue");
                revenueMap.put(type, revenue);
            }
        }

        return revenueMap;
    }

    public double getRevenueForType(String roomType) {
        try {
            Map<String, Double> map = getRevenueByRoomType();
            return map.getOrDefault(roomType.toUpperCase(), 0.0);
        } catch (Exception e) {
            System.err.println("[RevenueManager] Error: " + e.getMessage());
            return 0.0;
        }
    }
}
