package com.hotel.management.service;

import com.hotel.management.db.DatabaseConnection;
import com.hotel.management.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BookingService implements Repository<Booking, Integer> {

    private static final String CHECK_CHECKOUT_FLAG_EXISTS_SQL =
        "SELECT COUNT(*) AS cnt "
            + "FROM information_schema.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() "
            + "AND TABLE_NAME = 'bookings' "
            + "AND COLUMN_NAME = 'is_checked_out'";

    private static final String ADD_CHECKOUT_FLAG_SQL =
        "ALTER TABLE bookings ADD COLUMN is_checked_out TINYINT(1) NOT NULL DEFAULT 0";

    @Override
    public Integer save(Booking booking) throws Exception {
        return createBooking(booking) ? 1 : -1;
    }

    @Override
    public List<Booking> findAll() throws Exception {
        return getAllBookings();
    }

    @Override
    public boolean deleteById(Integer id) throws Exception {
        return deleteBooking(id);
    }

    public boolean createBooking(Booking booking) throws Exception {
        ensureCheckoutFlagColumn();

        String sql = "INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, number_of_days, tax_percent, total_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, booking.getCustomerId());
            stmt.setInt(2, booking.getRoomId());
            stmt.setDate(3, java.sql.Date.valueOf(booking.getCheckInDate()));
            stmt.setDate(4, java.sql.Date.valueOf(booking.getCheckOutDate()));
            stmt.setInt(5, booking.getNumberOfDays());
            stmt.setDouble(6, booking.getTaxPercent());
            stmt.setDouble(7, booking.getTotalAmount());

            return stmt.executeUpdate() > 0;
        }
    }

    public List<Booking> getAllBookings() throws Exception {
        ensureCheckoutFlagColumn();

        String sql = "SELECT booking_id, customer_id, room_id, check_in_date, check_out_date, "
                + "number_of_days, tax_percent, total_amount "
                + "FROM bookings "
                + "WHERE is_checked_out = 0 "
                + "ORDER BY booking_id DESC";
        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bookings.add(new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in_date").toLocalDate(),
                        rs.getDate("check_out_date").toLocalDate(),
                        rs.getInt("number_of_days"),
                        rs.getDouble("tax_percent"),
                        rs.getDouble("total_amount")
                ));
            }
        }
        return bookings;
    }

    public boolean markCheckedOut(int bookingId) throws Exception {
        ensureCheckoutFlagColumn();

        String sql = "UPDATE bookings SET is_checked_out = 1 WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Booking getBookingById(int bookingId) throws Exception {
        String sql = "SELECT booking_id, customer_id, room_id, check_in_date, check_out_date, "
                + "number_of_days, tax_percent, total_amount FROM bookings WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Booking(
                            rs.getInt("booking_id"),
                            rs.getInt("customer_id"),
                            rs.getInt("room_id"),
                            rs.getDate("check_in_date").toLocalDate(),
                            rs.getDate("check_out_date").toLocalDate(),
                            rs.getInt("number_of_days"),
                            rs.getDouble("tax_percent"),
                            rs.getDouble("total_amount")
                    );
                }
            }
        }
        return null;
    }

    public boolean deleteBooking(int bookingId) throws Exception {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;
        }
    }

    private void ensureCheckoutFlagColumn() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(CHECK_CHECKOUT_FLAG_EXISTS_SQL);
             ResultSet rs = checkStmt.executeQuery()) {

            boolean exists = false;
            if (rs.next()) {
                exists = rs.getInt("cnt") > 0;
            }

            if (!exists) {
                try (PreparedStatement addStmt = conn.prepareStatement(ADD_CHECKOUT_FLAG_SQL)) {
                    addStmt.execute();
                }
            }
        }
    }
}
