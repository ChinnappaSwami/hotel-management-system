package com.hotel.management.service;

import com.hotel.management.db.DatabaseConnection;
import com.hotel.management.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoomService {

    public List<Room> getAvailableRooms(String roomType) throws Exception {
        String sql = "SELECT room_id, room_number, room_type, price_per_day, status "
                + "FROM rooms WHERE status = 'AVAILABLE' AND room_type = ? ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, roomType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        }
        return rooms;
    }

    public List<Room> getAllRooms() throws Exception {
        String sql = "SELECT room_id, room_number, room_type, price_per_day, status FROM rooms ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        }
        return rooms;
    }

    public Room getRoomById(int roomId) throws Exception {
        String sql = "SELECT room_id, room_number, room_type, price_per_day, status FROM rooms WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        }
        return null;
    }

    public boolean updateRoomStatus(int roomId, String status) throws Exception {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, roomId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean addRoom(String roomNumber, String roomType, double pricePerDay) throws Exception {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_day, status) VALUES (?, ?, ?, 'AVAILABLE')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            stmt.setString(2, roomType.toUpperCase());
            stmt.setDouble(3, pricePerDay);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteRoom(int roomId) throws Exception {
        String sql = "DELETE FROM rooms WHERE room_id = ? AND status = 'AVAILABLE'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<String> getDistinctRoomTypes() throws Exception {
        String sql = "SELECT DISTINCT room_type FROM rooms ORDER BY room_type";
        List<String> types = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                types.add(rs.getString("room_type"));
            }
        }
        return types;
    }

    private Room mapRoom(ResultSet rs) throws Exception {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getString("room_type"),
                rs.getDouble("price_per_day"),
                rs.getString("status")
        );
    }
}
