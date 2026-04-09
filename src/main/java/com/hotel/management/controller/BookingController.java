package com.hotel.management.controller;

import com.hotel.management.model.Booking;
import com.hotel.management.model.Customer;
import com.hotel.management.model.Room;
import com.hotel.management.service.BillingService;
import com.hotel.management.service.BookingService;
import com.hotel.management.service.CustomerService;
import com.hotel.management.service.RoomService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingController {

    // ── Customer tab ──────────────────────────────────────────────────────
    @FXML
    private TextField customerNameField;
    @FXML
    private TextField customerEmailField;
    @FXML
    private TextField customerPhoneField;
    @FXML
    private TextField customerAddressField;
    @FXML
    private TextField customerSearchField; // NEW: search bar
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Integer> customerIdCol;
    @FXML
    private TableColumn<Customer, String> customerNameCol;
    @FXML
    private TableColumn<Customer, String> customerEmailCol;
    @FXML
    private TableColumn<Customer, String> customerPhoneCol;

    // ── Room tab ──────────────────────────────────────────────────────────
    @FXML
    private TextField addRoomNumberField;
    @FXML
    private ComboBox<String> addRoomTypeCombo; // Changed: was TextField
    @FXML
    private TextField addRoomPriceField;
    @FXML
    private TextField roomSearchField; // NEW: search bar
    @FXML
    private TableView<Room> roomTable;
    @FXML
    private TableColumn<Room, Integer> roomIdCol;
    @FXML
    private TableColumn<Room, String> roomNumberCol;
    @FXML
    private TableColumn<Room, String> roomTypeCol;
    @FXML
    private TableColumn<Room, Double> roomPriceCol;
    @FXML
    private TableColumn<Room, String> roomStatusCol;

    // ── Book and Bill tab ─────────────────────────────────────────────────
    @FXML
    private TextField bookingCustomerIdField;
    @FXML
    private ComboBox<String> bookingRoomTypeCombo;
    @FXML
    private ComboBox<Room> bookingRoomCombo;
    @FXML
    private DatePicker checkInDatePicker;
    @FXML
    private DatePicker checkOutDatePicker; // Changed: replaces daysField
    @FXML
    private CheckBox includeTaxCheck;
    @FXML
    private TextField taxPercentField;
    @FXML
    private Label bookingBillLabel;
    @FXML
    private TextField bookingSearchField; // NEW: search bar
    @FXML
    private TableView<Booking> bookingTable;
    @FXML
    private TableColumn<Booking, Integer> bookingIdCol;
    @FXML
    private TableColumn<Booking, Integer> bookingCustomerCol;
    @FXML
    private TableColumn<Booking, Integer> bookingRoomCol;
    @FXML
    private TableColumn<Booking, Integer> bookingDaysCol;
    @FXML
    private TableColumn<Booking, Double> bookingTotalCol;

    // ── Refresh button ────────────────────────────────────────────────────
    @FXML
    private Button refreshBtn;

    // ── Services ──────────────────────────────────────────────────────────
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final BookingService bookingService = new BookingService();

    // ── Backing lists (for search filtering) ─────────────────────────────
    private ObservableList<Customer> allCustomers = FXCollections.observableArrayList();
    private ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private ObservableList<Booking> allBookings = FXCollections.observableArrayList();

    // ── Thread pool for background DB tasks ─────────────────────────────
    // Uses a cached thread pool so we don't create more threads than needed
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "HotelMgmt-Worker");
        t.setDaemon(true); // daemon threads auto-stop when JVM exits
        return t;
    });

    // ─────────────────────── INITIALISE ──────────────────────────────────

    @FXML
    public void initialize() {
        setupTables();
        setupCombos();
        setupSearchBars();
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
        // Populate the room-type dropdown in the Add Room form
        if (addRoomTypeCombo != null) {
            addRoomTypeCombo.setItems(FXCollections.observableArrayList("SINGLE", "DOUBLE", "SUITE"));
            addRoomTypeCombo.getSelectionModel().selectFirst();
        }

        loadRoomTypeCombos();

        checkInDatePicker.setValue(LocalDate.now());
        if (checkOutDatePicker != null) {
            checkOutDatePicker.setValue(LocalDate.now().plusDays(1));
        }
        taxPercentField.setText("12");

        bookingRoomTypeCombo.setOnAction(e -> loadAvailableRoomsForBooking());
        includeTaxCheck.setOnAction(e -> taxPercentField.setDisable(!includeTaxCheck.isSelected()));
        taxPercentField.setDisable(true);
    }

    /** Wire up real-time search filtering on all three tables. */
    private void setupSearchBars() {
        // ── Customer search by Name ───────────────────────────────────────
        if (customerSearchField != null) {
            FilteredList<Customer> filteredCustomers = new FilteredList<>(allCustomers, c -> true);
            customerSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredCustomers.setPredicate(cust -> {
                    if (newVal == null || newVal.trim().isEmpty())
                        return true;
                    String lower = newVal.toLowerCase();
                    return cust.getFullName().toLowerCase().contains(lower);
                });
            });
            customerTable.setItems(filteredCustomers);
        }

        // ── Room search by Room Number ────────────────────────────────────
        if (roomSearchField != null) {
            FilteredList<Room> filteredRooms = new FilteredList<>(allRooms, r -> true);
            roomSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredRooms.setPredicate(room -> {
                    if (newVal == null || newVal.trim().isEmpty())
                        return true;
                    return room.getRoomNumber().toLowerCase().contains(newVal.toLowerCase());
                });
            });
            roomTable.setItems(filteredRooms);
        }

        // ── Booking search by Booking ID ──────────────────────────────────
        if (bookingSearchField != null) {
            FilteredList<Booking> filteredBookings = new FilteredList<>(allBookings, b -> true);
            bookingSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredBookings.setPredicate(booking -> {
                    if (newVal == null || newVal.trim().isEmpty())
                        return true;
                    return String.valueOf(booking.getId()).contains(newVal.trim());
                });
            });
            bookingTable.setItems(filteredBookings);
        }
    }

    // ─────────────────────── CUSTOMER ACTIONS ────────────────────────────

    @FXML
    private void onRegisterCustomer() {
        String name = customerNameField.getText().trim();
        String email = customerEmailField.getText().trim();
        String phone = customerPhoneField.getText().trim();
        String address = customerAddressField.getText().trim();

        // Validation ──────────────────────────────────────────────────────
        if (isBlank(name) || isBlank(phone)) {
            showError("Name and phone are required.");
            return;
        }
        // Email constraint: must contain '@' and end with '.com'
        if (!isBlank(email) && (!email.contains("@") || !email.endsWith(".com"))) {
            showError("Email must contain '@' and end with '.com'  (e.g. user@mail.com).");
            return;
        }
        // Phone constraint: exactly 10 digits
        if (!phone.matches("\\d{10}")) {
            showError("Phone number must be exactly 10 digits.");
            return;
        }

        Customer customer = new Customer(name, email, phone, address);

        // Multithreaded save ──────────────────────────────────────────────
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                return customerService.addCustomer(customer);
            }
        };
        task.setOnSucceeded(e -> {
            int customerId = task.getValue();
            if (customerId > 0) {
                bookingCustomerIdField.setText(String.valueOf(customerId));
                clearCustomerForm();
                loadCustomers();
            } else {
                showError("Unable to save customer.");
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    @FXML
    private void onDeleteCustomer() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a customer from the table first.");
            return;
        }

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return customerService.deleteCustomer(selected.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue())
                loadAllData();
            else
                showError("Could not delete customer. They may have active bookings.");
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    // ─────────────────────── ROOM ACTIONS ────────────────────────────────

    @FXML
    private void onAddRoom() {
        String roomNumber = addRoomNumberField.getText().trim();
        // Get type from ComboBox if present, else fall back gracefully
        String roomType = (addRoomTypeCombo != null && addRoomTypeCombo.getValue() != null)
                ? addRoomTypeCombo.getValue().toUpperCase()
                : "SINGLE";
        String priceText = addRoomPriceField.getText().trim();

        if (isBlank(roomNumber) || isBlank(priceText)) {
            showError("Room number and price are required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException ex) {
            showError("Price must be a valid number.");
            return;
        }
        if (price <= 0) {
            showError("Price must be greater than 0.");
            return;
        }

        final double finalPrice = price;
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return roomService.addRoom(roomNumber, roomType, finalPrice);
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                addRoomNumberField.clear();
                addRoomPriceField.clear();
                if (addRoomTypeCombo != null)
                    addRoomTypeCombo.getSelectionModel().selectFirst();
                loadAllData();
            } else {
                showError("Unable to add room. Check if room number already exists.");
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    @FXML
    private void onDeleteRoom() {
        Room selected = roomTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a room from the table first.");
            return;
        }

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return roomService.deleteRoom(selected.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue())
                loadAllData();
            else
                showError("Could not delete room. It may be currently booked.");
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    // ─────────────────────── BOOKING ACTIONS ─────────────────────────────

    @FXML
    private void onLoadAvailableRooms() {
        loadAvailableRoomsForBooking();
        loadRooms();
    }

    private void loadAvailableRoomsForBooking() {
        String selectedType = bookingRoomTypeCombo.getValue();
        if (selectedType == null)
            return;

        Task<List<Room>> task = new Task<>() {
            @Override
            protected List<Room> call() throws Exception {
                return roomService.getAvailableRooms(selectedType);
            }
        };
        task.setOnSucceeded(e -> {
            List<Room> rooms = task.getValue();
            bookingRoomCombo.setItems(FXCollections.observableArrayList(rooms));
            if (!rooms.isEmpty())
                bookingRoomCombo.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    /**
     * Computes number of days from check-in to check-out date. Returns -1 if
     * invalid.
     */
    private int computeDays() {
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = (checkOutDatePicker != null) ? checkOutDatePicker.getValue() : null;
        if (checkIn == null || checkOut == null)
            return -1;
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        return (days > 0) ? (int) days : -1;
    }

    @FXML
    private void onPreviewBookingBill() {
        Room selectedRoom = bookingRoomCombo.getValue();
        if (selectedRoom == null) {
            showError("Select an available room first.");
            return;
        }
        int days = computeDays();
        if (days <= 0) {
            showError("Check-out date must be after Check-in date.");
            return;
        }

        boolean includeTax = includeTaxCheck.isSelected();
        double tax;
        try {
            tax = includeTax ? Double.parseDouble(taxPercentField.getText().trim()) : 0.0;
        } catch (NumberFormatException ex) {
            showError("Enter a valid tax percentage.");
            return;
        }

        double total = BillingService.calculateTotal(selectedRoom.getPricePerDay(), days, includeTax, tax);
        bookingBillLabel.setText(String.format("Amount: Rs. %.2f  (%d day(s))", total, days));
    }

    @FXML
    private void onBookRoom() {
        int customerId;
        try {
            customerId = Integer.parseInt(bookingCustomerIdField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Customer ID must be a valid number.");
            return;
        }

        Room selectedRoom = bookingRoomCombo.getValue();
        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = (checkOutDatePicker != null) ? checkOutDatePicker.getValue() : null;
        int days = computeDays();

        if (selectedRoom == null || checkIn == null || checkOut == null || days <= 0) {
            showError("Fill all booking fields correctly. Check-out must be after Check-in.");
            return;
        }
        if (!"AVAILABLE".equalsIgnoreCase(selectedRoom.getStatus())) {
            showError("Selected room is not available.");
            return;
        }

        boolean includeTax = includeTaxCheck.isSelected();
        double tax;
        try {
            tax = includeTax ? Double.parseDouble(taxPercentField.getText().trim()) : 0.0;
        } catch (NumberFormatException ex) {
            showError("Enter a valid tax percentage.");
            return;
        }

        double total = BillingService.calculateTotal(selectedRoom.getPricePerDay(), days, includeTax, tax);
        Booking booking = new Booking(customerId, selectedRoom.getId(), checkIn, checkOut, days, tax, total);
        final int finalDays = days;
        final double finalTotal = total;

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                boolean saved = bookingService.createBooking(booking);
                if (saved)
                    roomService.updateRoomStatus(selectedRoom.getId(), "BOOKED");
                return saved;
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                bookingBillLabel.setText(String.format("Amount: Rs. %.2f  (%d day(s))", finalTotal, finalDays));
                loadAllData();
            } else {
                showError("Booking could not be saved.");
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    // ─────────────────────── CHECKOUT (from table) ────────────────────────

    @FXML
    private void onCheckOut() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a booking from the table first.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                roomService.updateRoomStatus(selected.getRoomId(), "AVAILABLE");
                bookingService.deleteBooking(selected.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadAllData());
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    // ─────────────────────── BILL POPUP ──────────────────────────────────

    @FXML
    private void onShowBill() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Please select a booking from the table first.");
            return;
        }

        Task<Room> task = new Task<>() {
            @Override
            protected Room call() throws Exception {
                return roomService.getRoomById(selected.getRoomId());
            }
        };
        task.setOnSucceeded(e -> showBillPopup(selected, task.getValue()));
        task.setOnFailed(e -> showBillPopup(selected, null));
        executor.submit(task);
    }

    private void showBillPopup(Booking selected, Room room) {
        String roomNo = (room != null) ? room.getRoomNumber() : String.valueOf(selected.getRoomId());
        String roomType = (room != null) ? room.getRoomType() : "N/A";
        double pricePerDay = (room != null) ? room.getPricePerDay() : 0.0;

        StringBuilder bill = new StringBuilder();
        bill.append("==========================================\n");
        bill.append("              HOTEL BILL\n");
        bill.append("==========================================\n\n");
        bill.append(String.format("  Booking ID     :  %d%n", selected.getId()));
        bill.append(String.format("  Customer ID    :  %d%n", selected.getCustomerId()));
        bill.append(String.format("  Room No        :  %s%n", roomNo));
        bill.append(String.format("  Room Type      :  %s%n", roomType));
        bill.append(String.format("  Check-in       :  %s%n", selected.getCheckInDate()));
        bill.append(String.format("  Check-out      :  %s%n", selected.getCheckOutDate()));
        bill.append(String.format("  No. of Days    :  %d%n", selected.getNumberOfDays()));
        bill.append(String.format("  Price / Day    :  Rs. %.2f%n", pricePerDay));
        bill.append(String.format("  Subtotal       :  Rs. %.2f%n", pricePerDay * selected.getNumberOfDays()));
        bill.append(String.format("  Tax            :  %.2f%%%n", selected.getTaxPercent()));
        bill.append("\n──────────────────────────────────────────\n");
        bill.append(String.format("  TOTAL AMOUNT   :  Rs. %.2f%n", selected.getTotalAmount()));
        bill.append("──────────────────────────────────────────\n");

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
                        + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 3, 0, 0, 1);");
        closeBtn.setOnAction(e -> popup.close());

        VBox layout = new VBox(16, billLabel, closeBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.setStyle(
                "-fx-background-color: #f0faf4; -fx-border-color: #a8d5ba; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        Scene scene = new Scene(layout);
        popup.setScene(scene);
        popup.setResizable(false);
        popup.showAndWait();
    }

    // ─────────────────────── REFRESH ─────────────────────────────────────

    @FXML
    private void onRefreshAll() {
        loadAllData();
    }

    // ─────────────────────── DATA LOADERS (threaded) ─────────────────────

    private void loadAllData() {
        loadCustomers();
        loadRooms();
        loadBookings();
        loadAvailableRoomsForBooking();
        loadRoomTypeCombos();
    }

    private void loadRoomTypeCombos() {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return roomService.getDistinctRoomTypes();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> types = task.getValue();
            if (types.isEmpty()) {
                types = List.of("SINGLE", "DOUBLE", "SUITE");
            }
            String prevBooking = bookingRoomTypeCombo.getValue();
            ObservableList<String> roomTypes = FXCollections.observableArrayList(types);
            bookingRoomTypeCombo.setItems(roomTypes);
            if (prevBooking != null && types.contains(prevBooking)) {
                bookingRoomTypeCombo.setValue(prevBooking);
            } else {
                bookingRoomTypeCombo.getSelectionModel().selectFirst();
            }
        });
        task.setOnFailed(e -> {
            bookingRoomTypeCombo.setItems(FXCollections.observableArrayList("SINGLE", "DOUBLE", "SUITE"));
            bookingRoomTypeCombo.getSelectionModel().selectFirst();
        });
        executor.submit(task);
    }

    private void loadCustomers() {
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() throws Exception {
                return customerService.getAllCustomers();
            }
        };
        task.setOnSucceeded(e -> {
            allCustomers.setAll(task.getValue());
            // If no search filter is wired yet, set items directly as fallback
            if (customerSearchField == null) {
                customerTable.setItems(allCustomers);
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private void loadRooms() {
        Task<List<Room>> task = new Task<>() {
            @Override
            protected List<Room> call() throws Exception {
                return roomService.getAllRooms();
            }
        };
        task.setOnSucceeded(e -> {
            allRooms.setAll(task.getValue());
            if (roomSearchField == null) {
                roomTable.setItems(allRooms);
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private void loadBookings() {
        Task<List<Booking>> task = new Task<>() {
            @Override
            protected List<Booking> call() throws Exception {
                return bookingService.getAllBookings();
            }
        };
        task.setOnSucceeded(e -> {
            allBookings.setAll(task.getValue());
            if (bookingSearchField == null) {
                bookingTable.setItems(allBookings);
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    // ─────────────────────── HELPERS ─────────────────────────────────────

    private void clearCustomerForm() {
        customerNameField.clear();
        customerEmailField.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
    }

    private void showError(String message) {
        // Always run UI changes on the FX Application Thread
        Runnable show = () -> {
            System.err.println("[ERROR] " + message);

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
                            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 3, 0, 0, 1);");
            okBtn.setOnAction(e -> popup.close());

            VBox layout = new VBox(12, msgLabel, okBtn);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));
            layout.setStyle(
                    "-fx-background-color: #fff5f5; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;");

            popup.setScene(new Scene(layout));
            popup.setResizable(false);
            popup.showAndWait();
        };

        if (Platform.isFxApplicationThread())
            show.run();
        else
            Platform.runLater(show);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
