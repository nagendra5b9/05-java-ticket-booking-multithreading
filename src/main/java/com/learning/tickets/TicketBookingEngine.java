package com.learning.tickets;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-Safe Core Engine for concurrent seat reservations.
 * Demonstrates:
 * - Thread contention handling
 * - ReentrantLock fine-grained locking
 * - Atomic variables (AtomicInteger) for lock-free counters
 * - Race condition prevention
 */
public class TicketBookingEngine {
    private final Map<String, Seat> seatInventory = new LinkedHashMap<>();
    private final AtomicInteger totalSuccessfulBookings = new AtomicInteger(0);
    private final AtomicInteger totalFailedAttempts = new AtomicInteger(0);
    private final AtomicInteger raceConditionsPrevented = new AtomicInteger(0);

    public TicketBookingEngine(int vipCount, int regularCount) {
        for (int i = 1; i <= vipCount; i++) {
            String id = "VIP-" + i;
            seatInventory.put(id, new Seat(id, "VIP", 75.00));
        }
        for (int i = 1; i <= regularCount; i++) {
            String id = "REG-" + i;
            seatInventory.put(id, new Seat(id, "REGULAR", 35.00));
        }
    }

    /**
     * Thread-Safe booking using fine-grained per-seat ReentrantLock.
     * Prevents global bottlenecks while guaranteeing zero double-booking.
     */
    public boolean tryBookSeat(String seatId, String customerName) {
        Seat seat = seatInventory.get(seatId);
        if (seat == null) {
            System.out.printf("[%s] Seat %s does not exist!\n", Thread.currentThread().getName(), seatId);
            return false;
        }

        // Acquire seat lock
        seat.getLock().lock();
        try {
            // Critical Section
            if (seat.isBooked()) {
                raceConditionsPrevented.incrementAndGet();
                totalFailedAttempts.incrementAndGet();
                System.out.printf(">> CONFLICT PREVENTED: [%s] tried booking %s, but already taken by %s!\n",
                        customerName, seatId, seat.getBookedByCustomer());
                return false;
            }

            // Simulate realistic processing / payment latency
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {}

            boolean success = seat.book(customerName);
            if (success) {
                totalSuccessfulBookings.incrementAndGet();
                System.out.printf(">> [SUCCESS] [%s] booked %s for %s (%s, $%.2f)\n",
                        Thread.currentThread().getName(), seatId, customerName, seat.getTier(), seat.getPrice());
                return true;
            } else {
                totalFailedAttempts.incrementAndGet();
                return false;
            }
        } finally {
            seat.getLock().unlock();
        }
    }

    public List<Seat> getAllSeats() {
        return new ArrayList<>(seatInventory.values());
    }

    public int getAvailableCount() {
        return (int) seatInventory.values().stream().filter(s -> !s.isBooked()).count();
    }

    public int getSuccessfulBookingsCount() { return totalSuccessfulBookings.get(); }
    public int getRaceConditionsPrevented() { return raceConditionsPrevented.get(); }
}
