package com.learning.tickets;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe representation of a single ticketed seat with dedicated Lock.
 */
public class Seat {
    private final String seatId;
    private final String tier; // VIP or REGULAR
    private final double price;
    private boolean isBooked;
    private String bookedByCustomer;
    private final ReentrantLock lock;

    public Seat(String seatId, String tier, double price) {
        this.seatId = seatId;
        this.tier = tier;
        this.price = price;
        this.isBooked = false;
        this.bookedByCustomer = null;
        this.lock = new ReentrantLock();
    }

    public String getSeatId() { return seatId; }
    public String getTier() { return tier; }
    public double getPrice() { return price; }
    public boolean isBooked() { return isBooked; }
    public String getBookedByCustomer() { return bookedByCustomer; }
    public ReentrantLock getLock() { return lock; }

    /**
     * Safely reserve seat under lock protection.
     */
    public boolean book(String customerName) {
        if (isBooked) {
            return false;
        }
        this.isBooked = true;
        this.bookedByCustomer = customerName;
        return true;
    }

    @Override
    public String toString() {
        return String.format("[%s (%s) - %s]", seatId, tier, (isBooked ? "BOOKED by " + bookedByCustomer : "AVAILABLE"));
    }
}
