package com.hotel.management.service;

import com.hotel.management.db.DatabaseConnection;
import com.hotel.management.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Customer operations.
 * Implements the generic {@link Repository} interface with Customer as T and Integer as ID.
 */
public class CustomerService implements Repository<Customer, Integer> {

    // ── Repository<Customer, Integer> contract ────────────────────────────

    /**
     * Saves a new customer and returns the generated customer_id, or -1 on failure.
     */
    @Override
    public Integer save(Customer customer) throws Exception {
        return addCustomer(customer);
    }

    /**
     * Returns all customers ordered by customer_id descending.
     */
    @Override
    public List<Customer> findAll() throws Exception {
        return getAllCustomers();
    }

    /**
     * Deletes a customer by ID (only if no active bookings exist).
     */
    @Override
    public boolean deleteById(Integer id) throws Exception {
        return deleteCustomer(id);
    }

    // ── Concrete methods ──────────────────────────────────────────────────

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
        // Only delete if customer has no active bookings
        String checkSql = "SELECT COUNT(*) FROM bookings WHERE customer_id = ?";
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
}
