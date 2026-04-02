package com.hotel.management.controller;

import com.hotel.management.model.Booking;
import com.hotel.management.model.Customer;
import com.hotel.management.model.Room;
import com.hotel.management.service.BillingService;
import com.hotel.management.service.BookingService;
import com.hotel.management.service.CustomerService;
import com.hotel.management.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class BookingController {

    // ── Customer tab ──
    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerAddressField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> customerIdCol;
    @FXML private TableColumn<Customer, String> customerNameCol;
    @FXML private TableColumn<Customer, String> customerEmailCol;
    @FXML private TableColumn<Customer, String> customerPhoneCol;

    // ── Room tab ──
    @FXML private TextField addRoomNumberField;
    @FXML private TextField addRoomTypeField;
    @FXML private TextField addRoomPriceField;
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, Integer> roomIdCol;
    @FXML private TableColumn<Room, String> roomNumberCol;
    @FXML private TableColumn<Room, String> roomTypeCol;
    @FXML private TableColumn<Room, Double> roomPriceCol;
    @FXML private TableColumn<Room, String> roomStatusCol;

    // ── Book and Bill tab ──
    @FXML private TextField bookingCustomerIdField;
    @FXML private ComboBox<String> bookingRoomTypeCombo;
    @FXML private ComboBox<Room> bookingRoomCombo;
    @FXML private DatePicker checkInDatePicker;
    @FXML private TextField daysField;
    @FXML private CheckBox includeTaxCheck;
    @FXML private TextField taxPercentField;
    @FXML private Label bookingBillLabel;
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, Integer> bookingIdCol;
    @FXML private TableColumn<Booking, Integer> bookingCustomerCol;
    @FXML private TableColumn<Booking, Integer> bookingRoomCol;
    @FXML private TableColumn<Booking, Integer> bookingDaysCol;
    @FXML private TableColumn<Booking, Double> bookingTotalCol;

    // ── Refresh button ──
    @FXML private Button refreshBtn;

    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final BookingService bookingService = new BookingService();

    // ─────────────────────── INITIALISE ───────────────────────

    @FXML
    public void initialize() {
        setupTables();
        setupCombos();
        loadAllData();
    }

    private void setupTables() {
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        customerEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        customerPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        roomIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomPriceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerDay"));
        roomStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        bookingIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookingCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        bookingRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        bookingDaysCol.setCellValueFactory(new PropertyValueFactory<>("numberOfDays"));
        bookingTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    }

    private void setupCombos() {
        loadRoomTypeCombos();

        checkInDatePicker.setValue(LocalDate.now());
        daysField.setText("1");
        taxPercentField.setText("12");

        bookingRoomTypeCombo.setOnAction(e -> loadAvailableRoomsForBooking());
        includeTaxCheck.setOnAction(e -> taxPercentField.setDisable(!includeTaxCheck.isSelected()));

        taxPercentField.setDisable(true);
    }

    // ─────────────────────── CUSTOMER ACTIONS ───────────────────────

    @FXML
    private void onRegisterCustomer() {
        try {
            if (isBlank(customerNameField.getText()) || isBlank(customerPhoneField.getText())) {
                showError("Name and phone are required.");
                return;
            }

            Customer customer = new Customer(
                    customerNameField.getText().trim(),
                    customerEmailField.getText().trim(),
                    customerPhoneField.getText().trim(),
                    customerAddressField.getText().trim()
            );

            int customerId = customerService.addCustomer(customer);
            if (customerId > 0) {
                bookingCustomerIdField.setText(String.valueOf(customerId));
                clearCustomerForm();
                loadCustomers();
            } else {
                showError("Unable to save customer.");
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onDeleteCustomer() {
        try {
            Customer selected = customerTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a customer from the table first.");
                return;
            }

            boolean deleted = customerService.deleteCustomer(selected.getId());
            if (deleted) {
                loadAllData();
            } else {
                showError("Could not delete customer. They may have active bookings.");
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─────────────────────── ROOM ACTIONS ───────────────────────

    @FXML
    private void onAddRoom() {
        try {
            if (isBlank(addRoomNumberField.getText()) || isBlank(addRoomTypeField.getText())
                    || isBlank(addRoomPriceField.getText())) {
                showError("Room number, type, and price are required.");
                return;
            }

            String roomNumber = addRoomNumberField.getText().trim();
            String roomType = addRoomTypeField.getText().trim().toUpperCase();
            double price = Double.parseDouble(addRoomPriceField.getText().trim());

            if (price <= 0) {
                showError("Price must be greater than 0.");
                return;
            }

            boolean added = roomService.addRoom(roomNumber, roomType, price);
            if (added) {
                addRoomNumberField.clear();
                addRoomTypeField.clear();
                addRoomPriceField.clear();
                loadAllData();
            } else {
                showError("Unable to add room. Check if room number already exists.");
            }
        } catch (NumberFormatException ex) {
            showError("Price must be a valid number.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onDeleteRoom() {
        try {
            Room selected = roomTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a room from the table first.");
                return;
            }

            boolean deleted = roomService.deleteRoom(selected.getId());
            if (deleted) {
                loadAllData();
            } else {
                showError("Could not delete room. It may be currently booked.");
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─────────────────────── BOOKING ACTIONS ───────────────────────

    @FXML
    private void onLoadAvailableRooms() {
        loadAvailableRoomsForBooking();
        loadRooms();
    }

    private void loadAvailableRoomsForBooking() {
        try {
            String selectedType = bookingRoomTypeCombo.getValue();
            if (selectedType == null) {
                return;
            }
            List<Room> availableRooms = roomService.getAvailableRooms(selectedType);
            bookingRoomCombo.setItems(FXCollections.observableArrayList(availableRooms));
            if (!availableRooms.isEmpty()) {
                bookingRoomCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void onPreviewBookingBill() {
        try {
            Room selectedRoom = bookingRoomCombo.getValue();
            if (selectedRoom == null) {
                showError("Select an available room first.");
                return;
            }
            int days = Integer.parseInt(daysField.getText().trim());
            if (days <= 0) {
                showError("Days must be greater than 0.");
                return;
            }

            boolean includeTax = includeTaxCheck.isSelected();
            double tax = includeTax ? Double.parseDouble(taxPercentField.getText().trim()) : 0.0;

            double total = BillingService.calculateTotal(selectedRoom.getPricePerDay(), days, includeTax, tax);
            bookingBillLabel.setText(String.format("Amount: Rs. %.2f", total));
        } catch (NumberFormatException ex) {
            showError("Enter valid numbers for days and tax.");
        }
    }

    @FXML
    private void onBookRoom() {
        try {
            int customerId = Integer.parseInt(bookingCustomerIdField.getText().trim());
            Room selectedRoom = bookingRoomCombo.getValue();
            LocalDate checkIn = checkInDatePicker.getValue();
            int days = Integer.parseInt(daysField.getText().trim());

            if (selectedRoom == null || checkIn == null || days <= 0) {
                showError("Fill all booking fields correctly.");
                return;
            }
            if (!"AVAILABLE".equalsIgnoreCase(selectedRoom.getStatus())) {
                showError("Selected room is not available.");
                return;
            }

            boolean includeTax = includeTaxCheck.isSelected();
            double tax = includeTax ? Double.parseDouble(taxPercentField.getText().trim()) : 0.0;
            double total = BillingService.calculateTotal(selectedRoom.getPricePerDay(), days, includeTax, tax);
            LocalDate checkOut = checkIn.plusDays(days);

            Booking booking = new Booking(customerId, selectedRoom.getId(), checkIn, checkOut, days, tax, total);
            boolean bookingSaved = bookingService.createBooking(booking);

            if (bookingSaved) {
                roomService.updateRoomStatus(selectedRoom.getId(), "BOOKED");
                bookingBillLabel.setText(String.format("Amount: Rs. %.2f", total));
                loadAllData();
            } else {
                showError("Booking could not be saved.");
            }
        } catch (NumberFormatException ex) {
            showError("Customer ID, days, and tax must be valid numbers.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─────────────────────── CHECKOUT (from table) ───────────────────────

    @FXML
    private void onCheckOut() {
        try {
            Booking selected = bookingTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a booking from the table first.");
                return;
            }

            int bookingId = selected.getId();

            // Free the room
            roomService.updateRoomStatus(selected.getRoomId(), "AVAILABLE");

            // Remove the booking
            bookingService.deleteBooking(bookingId);

            loadAllData();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─────────────────────── BILL POPUP ───────────────────────

    @FXML
    private void onShowBill() {
        try {
            Booking selected = bookingTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("Please select a booking from the table first.");
                return;
            }

            // Look up room details
            Room room = roomService.getRoomById(selected.getRoomId());
            String roomNo = (room != null) ? room.getRoomNumber() : String.valueOf(selected.getRoomId());
            String roomType = (room != null) ? room.getRoomType() : "N/A";
            double pricePerDay = (room != null) ? room.getPricePerDay() : 0.0;

            // Build bill text
            StringBuilder bill = new StringBuilder();
            bill.append("==========================================\n");
            bill.append("              HOTEL BILL\n");
            bill.append("==========================================\n\n");
            bill.append(String.format("  Booking ID     :  %d\n", selected.getId()));
            bill.append(String.format("  Customer ID    :  %d\n", selected.getCustomerId()));
            bill.append(String.format("  Room No        :  %s\n", roomNo));
            bill.append(String.format("  Room Type      :  %s\n", roomType));
            bill.append(String.format("  Check-in       :  %s\n", selected.getCheckInDate()));
            bill.append(String.format("  Check-out      :  %s\n", selected.getCheckOutDate()));
            bill.append(String.format("  No. of Days    :  %d\n", selected.getNumberOfDays()));
            bill.append(String.format("  Price / Day    :  Rs. %.2f\n", pricePerDay));
            bill.append(String.format("  Subtotal       :  Rs. %.2f\n", pricePerDay * selected.getNumberOfDays()));
            bill.append(String.format("  Tax            :  %.2f%%\n", selected.getTaxPercent()));
            bill.append("\n──────────────────────────────────────────\n");
            bill.append(String.format("  TOTAL AMOUNT   :  Rs. %.2f\n", selected.getTotalAmount()));
            bill.append("──────────────────────────────────────────\n");

            // Create popup
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("Bill - Booking #" + selected.getId());

            Label billLabel = new Label(bill.toString());
            billLabel.setFont(Font.font("Consolas", FontWeight.NORMAL, 14));
            billLabel.setStyle("-fx-text-fill: #1a3c5e;");

            Button closeBtn = new Button("Close");
            closeBtn.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #2980b9, #1f6fa3);"
                    + "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 24 7 24;"
                    + "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #185a8a;"
                    + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 3, 0, 0, 1);"
            );
            closeBtn.setOnAction(e -> popup.close());

            VBox layout = new VBox(16, billLabel, closeBtn);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(24));
            layout.setStyle("-fx-background-color: #f0faf4; -fx-border-color: #a8d5ba; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

            Scene scene = new Scene(layout);
            popup.setScene(scene);
            popup.setResizable(false);
            popup.showAndWait();

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─────────────────────── REFRESH ───────────────────────

    @FXML
    private void onRefreshAll() {
        loadAllData();
    }

    // ─────────────────────── DATA LOADERS ───────────────────────

    private void loadAllData() {
        loadCustomers();
        loadRooms();
        loadBookings();
        loadAvailableRoomsForBooking();
        loadRoomTypeCombos();
    }

    private void loadRoomTypeCombos() {
        try {
            List<String> types = roomService.getDistinctRoomTypes();
            if (types.isEmpty()) {
                types.add("SINGLE");
                types.add("DOUBLE");
                types.add("SUITE");
            }
            String prevBooking = bookingRoomTypeCombo.getValue();

            ObservableList<String> roomTypes = FXCollections.observableArrayList(types);
            bookingRoomTypeCombo.setItems(roomTypes);

            if (prevBooking != null && types.contains(prevBooking)) {
                bookingRoomTypeCombo.setValue(prevBooking);
            } else {
                bookingRoomTypeCombo.getSelectionModel().selectFirst();
            }
        } catch (Exception ex) {
            ObservableList<String> fallback = FXCollections.observableArrayList("SINGLE", "DOUBLE", "SUITE");
            bookingRoomTypeCombo.setItems(fallback);
            bookingRoomTypeCombo.getSelectionModel().selectFirst();
        }
    }

    private void loadCustomers() {
        try {
            customerTable.setItems(FXCollections.observableArrayList(customerService.getAllCustomers()));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void loadRooms() {
        try {
            roomTable.setItems(FXCollections.observableArrayList(roomService.getAllRooms()));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void loadBookings() {
        try {
            bookingTable.setItems(FXCollections.observableArrayList(bookingService.getAllBookings()));
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // ─────────────────────── HELPERS ───────────────────────

    private void clearCustomerForm() {
        customerNameField.clear();
        customerEmailField.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
    }

    private void showError(String message) {
        // Inline status bar style error — no popup alerts
        System.err.println("[ERROR] " + message);

        // Show a temporary popup only for errors (lightweight)
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Error");

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        msgLabel.setStyle("-fx-text-fill: #c0392b;");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(350);

        Button okBtn = new Button("OK");
        okBtn.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #c0392b, #a93226);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 20 6 20;"
                + "-fx-background-radius: 5; -fx-border-radius: 5; -fx-cursor: hand;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 3, 0, 0, 1);"
        );
        okBtn.setOnAction(e -> popup.close());

        VBox layout = new VBox(12, msgLabel, okBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;");

        popup.setScene(new Scene(layout));
        popup.setResizable(false);
        popup.showAndWait();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
