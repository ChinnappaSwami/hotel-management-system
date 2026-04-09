package com.hotel.management.service;

import com.hotel.management.db.DatabaseConnection;
import com.hotel.management.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Booking operations.
 * Implements the generic {@link Repository} interface with Booking as T and Integer as ID.
 */
public class BookingService implements Repository<Booking, Integer> {

    // ── Repository<Booking, Integer> contract ─────────────────────────────

    /**
     * Saves a new booking. Returns 1 on success, -1 on failure.
     */
    @Override
    public Integer save(Booking booking) throws Exception {
        return createBooking(booking) ? 1 : -1;
    }

    /**
     * Returns all bookings ordered by booking_id descending.
     */
    @Override
    public List<Booking> findAll() throws Exception {
        return getAllBookings();
    }

    /**
     * Deletes a booking by its ID.
     */
    @Override
    public boolean deleteById(Integer id) throws Exception {
        return deleteBooking(id);
    }

    // ── Concrete methods ──────────────────────────────────────────────────

    public boolean createBooking(Booking booking) throws Exception {
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
        String sql = "SELECT booking_id, customer_id, room_id, check_in_date, check_out_date, "
                + "number_of_days, tax_percent, total_amount FROM bookings ORDER BY booking_id DESC";
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
}
