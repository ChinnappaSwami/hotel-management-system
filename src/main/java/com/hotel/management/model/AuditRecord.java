package com.hotel.management.model;

import java.io.Serializable;

public class AuditRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventType;

    private int roomNumber;

    private String customerName;

    private String timestamp;

    public AuditRecord(String eventType, int roomNumber, String customerName, String timestamp) {
        this.eventType    = eventType;
        this.roomNumber   = roomNumber;
        this.customerName = customerName;
        this.timestamp    = timestamp;
    }

    public String getEventType()    { return eventType; }
    public void   setEventType(String eventType) { this.eventType = eventType; }

    public int    getRoomNumber()   { return roomNumber; }
    public void   setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }

    public String getCustomerName() { return customerName; }
    public void   setCustomerName(String customerName) { this.customerName = customerName; }

    public String getTimestamp()    { return timestamp; }
    public void   setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + eventType
                + " | Room: " + roomNumber
                + " | Customer: " + customerName;
    }
}
