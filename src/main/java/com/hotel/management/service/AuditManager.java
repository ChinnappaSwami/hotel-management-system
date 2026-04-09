package com.hotel.management.service;

import com.hotel.management.model.AuditRecord;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditManager {

    private static final String AUDIT_FILE = "Audit.dat";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static AuditManager instance;

    public static synchronized AuditManager getInstance() {
        if (instance == null) {
            instance = new AuditManager();
        }
        return instance;
    }

    private AuditManager() {
    }

    public synchronized void log(String eventType, int roomNumber, String customerName) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditRecord record = new AuditRecord(eventType, roomNumber, customerName, timestamp);

        List<AuditRecord> records = deserialize();

        records.add(record);

        serialize(records);

        System.out.println("[AuditManager] Logged: " + record);
    }

    public synchronized List<AuditRecord> getAll() {
        return deserialize();
    }

    private void serialize(List<AuditRecord> records) {
        try (FileOutputStream fos = new FileOutputStream(AUDIT_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(records);

        } catch (IOException e) {
            System.err.println("[AuditManager] Failed to write Audit.dat: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<AuditRecord> deserialize() {
        File file = new File(AUDIT_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            return (List<AuditRecord>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[AuditManager] Failed to read Audit.dat: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
