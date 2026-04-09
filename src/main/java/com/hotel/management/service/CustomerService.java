package com.hotel.management.service;

import com.hotel.management.db.DatabaseConnection;
import com.hotel.management.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerService implements Repository<Customer, Integer> {

    private static final String CHECK_CHECKOUT_FLAG_EXISTS_SQL =
        "SELECT COUNT(*) AS cnt "
            + "FROM information_schema.COLUMNS "
            + "WHERE TABLE_SCHEMA = DATABASE() "
            + "AND TABLE_NAME = 'bookings' "
            + "AND COLUMN_NAME = 'is_checked_out'";

    private static final String ADD_CHECKOUT_FLAG_SQL =
        "ALTER TABLE bookings ADD COLUMN is_checked_out TINYINT(1) NOT NULL DEFAULT 0";

    @Override
    public Integer save(Customer customer) throws Exception {
        return addCustomer(customer);
    }

    @Override
    public List<Customer> findAll() throws Exception {
        return getAllCustomers();
    }

    @Override
    public boolean deleteById(Integer id) throws Exception {
        return deleteCustomer(id);
    }

    public int addCustomer(Customer customer) throws Exception {
        String sql = "INSERT INTO customers (full_name, email, phone, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getEmail());
            stmt.setString(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                return -1;
            }

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public List<Customer> getAllCustomers() throws Exception {
        String sql = "SELECT customer_id, full_name, email, phone, address FROM customers ORDER BY customer_id DESC";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        }
        return customers;
    }

    public boolean deleteCustomer(int customerId) throws Exception {
        ensureCheckoutFlagColumn();

        String checkSql = "SELECT COUNT(*) FROM bookings WHERE customer_id = ? AND is_checked_out = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, customerId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            }
        }

        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
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
