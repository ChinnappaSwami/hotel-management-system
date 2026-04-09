package com.hotel.management.controller;

import com.hotel.management.cleaning.CleaningManager;
import com.hotel.management.model.AuditRecord;
import com.hotel.management.model.Booking;
import com.hotel.management.model.Customer;
import com.hotel.management.model.Room;
import com.hotel.management.service.AuditManager;
import com.hotel.management.service.BillingService;
import com.hotel.management.service.BookingService;
import com.hotel.management.service.CustomerService;
import com.hotel.management.service.RevenueManager;
import com.hotel.management.service.RoomService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class BookingController {

    @FXML private TextField customerNameField;
    @FXML private TextField customerEmailField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerAddressField;
    @FXML private TextField customerSearchField;
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> customerIdCol;
    @FXML private TableColumn<Customer, String>  customerNameCol;
    @FXML private TableColumn<Customer, String>  customerEmailCol;
    @FXML private TableColumn<Customer, String>  customerPhoneCol;

    @FXML private TextField addRoomNumberField;
    @FXML private ComboBox<String> addRoomTypeCombo;
    @FXML private TextField addRoomPriceField;
    @FXML private TextField roomSearchField;
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, Integer> roomIdCol;
    @FXML private TableColumn<Room, String>  roomNumberCol;
    @FXML private TableColumn<Room, String>  roomTypeCol;
    @FXML private TableColumn<Room, Double>  roomPriceCol;
    @FXML private TableColumn<Room, String>  roomStatusCol;

    @FXML private TextField       bookingCustomerIdField;
    @FXML private ComboBox<String> bookingRoomTypeCombo;
    @FXML private ComboBox<Room>   bookingRoomCombo;
    @FXML private DatePicker       checkInDatePicker;
    @FXML private DatePicker       checkOutDatePicker;
    @FXML private CheckBox         includeTaxCheck;
    @FXML private TextField        taxPercentField;
    @FXML private Label            bookingBillLabel;
    @FXML private TextField        bookingSearchField;
    @FXML private TableView<Booking>  bookingTable;
    @FXML private TableColumn<Booking, Integer> bookingIdCol;
    @FXML private TableColumn<Booking, Integer> bookingCustomerCol;
    @FXML private TableColumn<Booking, Integer> bookingRoomCol;
    @FXML private TableColumn<Booking, Integer> bookingDaysCol;
    @FXML private TableColumn<Booking, Double>  bookingTotalCol;

    @FXML private TableView<Room>           cleaningTable;
    @FXML private TableColumn<Room, String> cleaningRoomNumberCol;
    @FXML private TableColumn<Room, String> cleaningRoomTypeCol;
    @FXML private TableColumn<Room, String> cleaningStatusCol;
    @FXML private TableColumn<Room, Void>   cleaningProgressCol;
    @FXML private Label                     cleaningStatusLabel;

    @FXML private BarChart<String, Number> singleRevenueChart;

    @FXML private BarChart<String, Number> doubleRevenueChart;

    @FXML private BarChart<String, Number> suiteRevenueChart;

    @FXML private TableView<AuditRecord>           auditTable;
    @FXML private TableColumn<AuditRecord, String> auditTimestampCol;
    @FXML private TableColumn<AuditRecord, String> auditEventCol;
    @FXML private TableColumn<AuditRecord, Integer> auditRoomCol;
    @FXML private TableColumn<AuditRecord, String> auditCustomerCol;

    @FXML private Button refreshBtn;

    private final CustomerService customerService = new CustomerService();
    private final RoomService     roomService     = new RoomService();
    private final BookingService  bookingService  = new BookingService();

    private final AuditManager   auditManager   = AuditManager.getInstance();

    private final RevenueManager revenueManager = RevenueManager.getInstance();

    private final ObservableList<Customer>    allCustomers  = FXCollections.observableArrayList();
    private final ObservableList<Room>        allRooms      = FXCollections.observableArrayList();
    private final ObservableList<Booking>     allBookings   = FXCollections.observableArrayList();
    private final ObservableList<Room>        cleaningRooms = FXCollections.observableArrayList();
    private final ObservableList<AuditRecord> auditRecords  = FXCollections.observableArrayList();

    private final Map<Integer, ProgressBar> cleaningProgressBars = new HashMap<>();

    private final CleaningManager cleaningManager = new CleaningManager();

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "HotelMgmt-Worker");
        t.setDaemon(true);
        return t;
    });

    @FXML
    public void initialize() {
        setupTables();
        setupCombos();
        setupSearchBars();
        setupCleaningTable();
        setupRoomRowColors();
        setupAuditTable();
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

    private void setupAuditTable() {
        auditTimestampCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        auditEventCol.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        auditRoomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        auditCustomerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        auditTable.setItems(auditRecords);
    }

    private void setupCleaningTable() {
        cleaningRoomNumberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        cleaningRoomTypeCol.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        cleaningStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        cleaningProgressCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Room room = getTableRow().getItem();
                    ProgressBar bar = cleaningProgressBars.get(room.getId());
                    if (bar != null) {
                        bar.setPrefWidth(160);
                        setGraphic(bar);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        cleaningTable.setItems(cleaningRooms);
    }

    private void setupRoomRowColors() {
        roomTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);
                getStyleClass().removeAll("row-available", "row-cleaning", "row-booked");
                if (!empty && room != null) {
                    switch (room.getStatus().toUpperCase()) {
                        case "AVAILABLE" -> getStyleClass().add("row-available");
                        case "CLEANING"  -> getStyleClass().add("row-cleaning");
                        case "BOOKED"    -> getStyleClass().add("row-booked");
                    }
                }
            }
        });
    }

    private void setupCombos() {
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

    private void setupSearchBars() {
        if (customerSearchField != null) {
            FilteredList<Customer> filtered = new FilteredList<>(allCustomers, c -> true);
            customerSearchField.textProperty().addListener((obs, o, n) ->
                    filtered.setPredicate(cust ->
                            isBlank(n) || cust.getFullName().toLowerCase().contains(n.toLowerCase())));
            customerTable.setItems(filtered);
        }
        if (roomSearchField != null) {
            FilteredList<Room> filtered = new FilteredList<>(allRooms, r -> true);
            roomSearchField.textProperty().addListener((obs, o, n) ->
                    filtered.setPredicate(room ->
                            isBlank(n) || room.getRoomNumber().toLowerCase().contains(n.toLowerCase())));
            roomTable.setItems(filtered);
        }
        if (bookingSearchField != null) {
            FilteredList<Booking> filtered = new FilteredList<>(allBookings, b -> true);
            bookingSearchField.textProperty().addListener((obs, o, n) ->
                    filtered.setPredicate(booking ->
                            isBlank(n) || String.valueOf(booking.getId()).contains(n.trim())));
            bookingTable.setItems(filtered);
        }
    }

    @FXML
    private void onRegisterCustomer() {
        String name    = customerNameField.getText().trim();
        String email   = customerEmailField.getText().trim();
        String phone   = customerPhoneField.getText().trim();
        String address = customerAddressField.getText().trim();

        if (isBlank(name) || isBlank(phone)) { showError("Name and phone are required."); return; }
        if (!isBlank(email) && (!email.contains("@") || !email.endsWith(".com"))) {
            showError("Email must contain '@' and end with '.com'  (e.g. user@mail.com)."); return;
        }
        if (!phone.matches("\\d{10}")) { showError("Phone number must be exactly 10 digits."); return; }

        Customer customer = new Customer(name, email, phone, address);
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                int newId = customerService.addCustomer(customer);
                if (newId > 0) {
                    auditManager.log("CUSTOMER_ADDED", 0, name);
                }
                return newId;
            }
        };
        task.setOnSucceeded(e -> {
            int id = task.getValue();
            if (id > 0) {
                bookingCustomerIdField.setText(String.valueOf(id));
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
        if (selected == null) { showError("Please select a customer from the table first."); return; }
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return customerService.deleteCustomer(selected.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) loadAllData();
            else showError("Could not delete customer. They may have active bookings.");
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    @FXML
    private void onAddRoom() {
        String roomNumber = addRoomNumberField.getText().trim();
        String roomType   = (addRoomTypeCombo != null && addRoomTypeCombo.getValue() != null)
                ? addRoomTypeCombo.getValue().toUpperCase() : "SINGLE";
        String priceText  = addRoomPriceField.getText().trim();

        if (isBlank(roomNumber) || isBlank(priceText)) { showError("Room number and price are required."); return; }
        double price;
        try { price = Double.parseDouble(priceText); }
        catch (NumberFormatException ex) { showError("Price must be a valid number."); return; }
        if (price <= 0) { showError("Price must be greater than 0."); return; }

        final double finalPrice = price;
        final String finalRoomType = roomType;
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                boolean added = roomService.addRoom(roomNumber, finalRoomType, finalPrice);
                if (added) {
                    int roomNum = 0;
                    try { roomNum = Integer.parseInt(roomNumber); } catch (NumberFormatException ignored) {}
                    auditManager.log("ROOM_ADDED", roomNum, "N/A");
                }
                return added;
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                addRoomNumberField.clear();
                addRoomPriceField.clear();
                if (addRoomTypeCombo != null) addRoomTypeCombo.getSelectionModel().selectFirst();
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
        if (selected == null) { showError("Please select a room from the table first."); return; }
        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return roomService.deleteRoom(selected.getId());
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) loadAllData();
            else showError("Could not delete room. It may be currently booked.");
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    @FXML
    private void onLoadAvailableRooms() { loadAvailableRoomsForBooking(); loadRooms(); }

    private void loadAvailableRoomsForBooking() {
        String selectedType = bookingRoomTypeCombo.getValue();
        if (selectedType == null) return;
        Task<List<Room>> task = new Task<>() {
            @Override protected List<Room> call() throws Exception {
                return roomService.getAvailableRooms(selectedType);
            }
        };
        task.setOnSucceeded(e -> {
            List<Room> rooms = task.getValue();
            bookingRoomCombo.setItems(FXCollections.observableArrayList(rooms));
            if (!rooms.isEmpty()) bookingRoomCombo.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private int computeDays() {
        LocalDate checkIn  = checkInDatePicker.getValue();
        LocalDate checkOut = (checkOutDatePicker != null) ? checkOutDatePicker.getValue() : null;
        if (checkIn == null || checkOut == null) return -1;
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        return (days > 0) ? (int) days : -1;
    }

    @FXML
    private void onPreviewBookingBill() {
        Room selectedRoom = bookingRoomCombo.getValue();
        if (selectedRoom == null) { showError("Select an available room first."); return; }
        int days = computeDays();
        if (days <= 0) { showError("Check-out date must be after Check-in date."); return; }
        boolean includeTax = includeTaxCheck.isSelected();
        double tax;
        try { tax = includeTax ? Double.parseDouble(taxPercentField.getText().trim()) : 0.0; }
        catch (NumberFormatException ex) { showError("Enter a valid tax percentage."); return; }
        double total = BillingService.calculateTotal(selectedRoom.getPricePerDay(), days, includeTax, tax);
        bookingBillLabel.setText(String.format("Amount: Rs. %.2f  (%d day(s))", total, days));
    }

    @FXML
    private void onBookRoom() {
        int customerId;
        try { customerId = Integer.parseInt(bookingCustomerIdField.getText().trim()); }
        catch (NumberFormatException ex) { showError("Customer ID must be a valid number."); return; }

        Room selectedRoom = bookingRoomCombo.getValue();
        LocalDate checkIn  = checkInDatePicker.getValue();
        LocalDate checkOut = (checkOutDatePicker != null) ? checkOutDatePicker.getValue() : null;
        int days = computeDays();

        if (selectedRoom == null || checkIn == null || checkOut == null || days <= 0) {
            showError("Fill all booking fields correctly. Check-out must be after Check-in."); return;
        }
        if (!"AVAILABLE".equalsIgnoreCase(selectedRoom.getStatus())) {
            showError("Selected room is not available."); return;
        }
        boolean includeTax = includeTaxCheck.isSelected();
        double tax;
        try { tax = includeTax ? Double.parseDouble(taxPercentField.getText().trim()) : 0.0; }
        catch (NumberFormatException ex) { showError("Enter a valid tax percentage."); return; }

        double total   = BillingService.calculateTotal(selectedRoom.getPricePerDay(), days, includeTax, tax);
        double revenue = selectedRoom.getPricePerDay() * days;

        Booking booking = new Booking(customerId, selectedRoom.getId(),
                checkIn, checkOut, days, tax, total, revenue);

        final int    finalDays   = days;
        final double finalTotal  = total;
        final int    finalCustId = customerId;
        final Room   finalRoom   = selectedRoom;

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                boolean saved = bookingService.createBooking(booking);
                if (saved) {
                    roomService.updateRoomStatus(finalRoom.getId(), "BOOKED");

                    int roomNum = 0;
                    try { roomNum = Integer.parseInt(finalRoom.getRoomNumber()); }
                    catch (NumberFormatException ignored) {}

                    String customerName = "Customer #" + finalCustId;
                    try {
                        List<com.hotel.management.model.Customer> allCusts =
                                customerService.getAllCustomers();
                        for (com.hotel.management.model.Customer c : allCusts) {
                            if (c.getId() == finalCustId) {
                                customerName = c.getFullName();
                                break;
                            }
                        }
                    } catch (Exception ignored) {}

                    auditManager.log("ROOM_BOOKED", roomNum, customerName);
                }
                return saved;
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                bookingBillLabel.setText(
                        String.format("Amount: Rs. %.2f  (%d day(s))", finalTotal, finalDays));
                loadAllData();
            } else {
                showError("Booking could not be saved.");
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    @FXML
    private void onCheckOut() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a booking from the table first."); return; }

        Task<Room> task = new Task<>() {
            @Override protected Room call() throws Exception {
                bookingService.markCheckedOut(selected.getId());

                roomService.updateRoomStatus(selected.getRoomId(), "CLEANING");
                Room room = roomService.getRoomById(selected.getRoomId());

                if (room != null) {
                    int roomNum = 0;
                    try { roomNum = Integer.parseInt(room.getRoomNumber()); }
                    catch (NumberFormatException ignored) {}
                    auditManager.log("ROOM_CHECKOUT", roomNum, "N/A");
                }
                return room;
            }
        };
        task.setOnSucceeded(e -> {
            Room room = task.getValue();
            if (room != null) {
                loadAllData();
                addToCleaningTable(room);
                startCleaning(room);
            }
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private void addToCleaningTable(Room room) {
        ProgressBar bar = new ProgressBar(0);
        bar.setPrefWidth(160);
        bar.setStyle("-fx-accent: #27ae60;");
        cleaningProgressBars.put(room.getId(), bar);
        cleaningRooms.add(room);
        cleaningTable.refresh();
        updateCleaningStatusLabel();
    }

    private void startCleaning(Room room) {
        ProgressBar bar = cleaningProgressBars.get(room.getId());

        Runnable onComplete = () -> {
            executor.submit(() -> {
                try {
                    roomService.updateRoomStatus(room.getId(), "AVAILABLE");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Platform.runLater(() -> {
                    cleaningRooms.remove(room);
                    cleaningProgressBars.remove(room.getId());
                    loadRooms();
                    loadAvailableRoomsForBooking();
                    updateCleaningStatusLabel();
                    System.out.println("[Controller] Room " + room.getRoomNumber() + " is now AVAILABLE.");
                });
            });
        };

        cleaningManager.submit(room, bar, onComplete);
        updateCleaningStatusLabel();
    }

    private void updateCleaningStatusLabel() {
        if (cleaningStatusLabel != null) {
            cleaningStatusLabel.setText(String.format(
                    "Active cleaners: %d / %d   ·   Queue: %d",
                    cleaningManager.getActiveCount(),
                    CleaningManager.MAX_CLEANERS,
                    cleaningManager.getQueueSize()));
        }
    }

    @FXML
    private void onRevenueTabSelected(Event event) {
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected()) {
            refreshRevenueCharts();
            refreshAuditTable();
        }
    }

    @FXML
    private void onRefreshRevenue() {
        refreshRevenueCharts();
        refreshAuditTable();
    }

    private void refreshRevenueCharts() {
        Task<Map<String, Double>> task = new Task<>() {
            @Override protected Map<String, Double> call() throws Exception {
                return revenueManager.getRevenueByRoomType();
            }
        };
        task.setOnSucceeded(e -> {
            Map<String, Double> data = task.getValue();
            updateChart(singleRevenueChart, "SINGLE", data.getOrDefault("SINGLE", 0.0));
            updateChart(doubleRevenueChart, "DOUBLE", data.getOrDefault("DOUBLE", 0.0));
            updateChart(suiteRevenueChart,  "SUITE",  data.getOrDefault("SUITE",  0.0));
        });
        task.setOnFailed(e -> System.err.println("[RevenueCharts] Failed: " + task.getException().getMessage()));
        executor.submit(task);
    }

    private void updateChart(BarChart<String, Number> chart, String label, double revenue) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        series.getData().add(new XYChart.Data<>(label, revenue));
        chart.getData().add(series);
        chart.setTitle(String.format("Rs. %.0f", revenue));
    }

    private void refreshAuditTable() {
        Task<List<AuditRecord>> task = new Task<>() {
            @Override protected List<AuditRecord> call() {
                return auditManager.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            ObservableList<AuditRecord> records =
                    FXCollections.observableArrayList(task.getValue());
            FXCollections.reverse(records);
            auditRecords.setAll(records);
        });
        executor.submit(task);
    }

    @FXML
    private void onShowBill() {
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Please select a booking from the table first."); return; }
        Task<Room> task = new Task<>() {
            @Override protected Room call() throws Exception {
                return roomService.getRoomById(selected.getRoomId());
            }
        };
        task.setOnSucceeded(e -> showBillPopup(selected, task.getValue()));
        task.setOnFailed(e -> showBillPopup(selected, null));
        executor.submit(task);
    }

    private void showBillPopup(Booking selected, Room room) {
        String roomNo      = (room != null) ? room.getRoomNumber() : String.valueOf(selected.getRoomId());
        String roomType    = (room != null) ? room.getRoomType() : "N/A";
        double pricePerDay = (room != null) ? room.getPricePerDay() : 0.0;

        StringBuilder bill = new StringBuilder();
        bill.append("==========================================\n");
        bill.append("              HOTEL BILL\n");
        bill.append("==========================================\n\n");
        bill.append(String.format("  Booking ID     :  %d%n",   selected.getId()));
        bill.append(String.format("  Customer ID    :  %d%n",   selected.getCustomerId()));
        bill.append(String.format("  Room No        :  %s%n",   roomNo));
        bill.append(String.format("  Room Type      :  %s%n",   roomType));
        bill.append(String.format("  Check-in       :  %s%n",   selected.getCheckInDate()));
        bill.append(String.format("  Check-out      :  %s%n",   selected.getCheckOutDate()));
        bill.append(String.format("  No. of Days    :  %d%n",   selected.getNumberOfDays()));
        bill.append(String.format("  Price / Day    :  Rs. %.2f%n", pricePerDay));
        bill.append(String.format("  Base Revenue   :  Rs. %.2f%n",
                pricePerDay * selected.getNumberOfDays()));
        bill.append(String.format("  Tax            :  %.2f%%%n",   selected.getTaxPercent()));
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
        closeBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #2980b9, #1f6fa3);"
                + "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 7 24 7 24;"
                + "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #185a8a;"
                + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 3, 0, 0, 1);");
        closeBtn.setOnAction(e -> popup.close());

        VBox layout = new VBox(16, billLabel, closeBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(24));
        layout.setStyle("-fx-background-color: #f0faf4; -fx-border-color: #a8d5ba;"
                + "-fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");

        popup.setScene(new Scene(layout));
        popup.setResizable(false);
        popup.showAndWait();
    }

    @FXML
    private void onRefreshAll() {
        loadAllData();
        updateCleaningStatusLabel();
    }

    private void loadAllData() {
        loadCustomers();
        loadRooms();
        loadBookings();
        loadAvailableRoomsForBooking();
        loadRoomTypeCombos();
    }

    private void loadRoomTypeCombos() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return roomService.getDistinctRoomTypes();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> types = task.getValue().isEmpty()
                    ? List.of("SINGLE", "DOUBLE", "SUITE") : task.getValue();
            String prev = bookingRoomTypeCombo.getValue();
            bookingRoomTypeCombo.setItems(FXCollections.observableArrayList(types));
            if (prev != null && types.contains(prev)) bookingRoomTypeCombo.setValue(prev);
            else bookingRoomTypeCombo.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> {
            bookingRoomTypeCombo.setItems(FXCollections.observableArrayList("SINGLE", "DOUBLE", "SUITE"));
            bookingRoomTypeCombo.getSelectionModel().selectFirst();
        });
        executor.submit(task);
    }

    private void loadCustomers() {
        Task<List<Customer>> task = new Task<>() {
            @Override protected List<Customer> call() throws Exception {
                return customerService.getAllCustomers();
            }
        };
        task.setOnSucceeded(e -> {
            allCustomers.setAll(task.getValue());
            if (customerSearchField == null) customerTable.setItems(allCustomers);
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private void loadRooms() {
        Task<List<Room>> task = new Task<>() {
            @Override protected List<Room> call() throws Exception {
                return roomService.getAllRooms();
            }
        };
        task.setOnSucceeded(e -> {
            allRooms.setAll(task.getValue());
            if (roomSearchField == null) roomTable.setItems(allRooms);
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private void loadBookings() {
        Task<List<Booking>> task = new Task<>() {
            @Override protected List<Booking> call() throws Exception {
                return bookingService.getAllBookings();
            }
        };
        task.setOnSucceeded(e -> {
            allBookings.setAll(task.getValue());
            if (bookingSearchField == null) bookingTable.setItems(allBookings);
        });
        task.setOnFailed(e -> showError(task.getException().getMessage()));
        executor.submit(task);
    }

    private void clearCustomerForm() {
        customerNameField.clear();
        customerEmailField.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
    }

    private void showError(String message) {
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
            okBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #c0392b, #a93226);"
                    + "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 20 6 20;"
                    + "-fx-background-radius: 5; -fx-border-radius: 5; -fx-cursor: hand;"
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.20), 3, 0, 0, 1);");
            okBtn.setOnAction(e -> popup.close());
            VBox layout = new VBox(12, msgLabel, okBtn);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));
            layout.setStyle("-fx-background-color: #fff5f5; -fx-border-color: #e74c3c;"
                    + "-fx-border-width: 2; -fx-border-radius: 6; -fx-background-radius: 6;");
            popup.setScene(new Scene(layout));
            popup.setResizable(false);
            popup.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
