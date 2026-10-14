package com.learning.tickets;

import java.util.Random;

/**
 * Runnable task simulating an individual customer attempting to book a seat.
 */
public class CustomerBookingTask implements Runnable {
    private final TicketBookingEngine engine;
    private final String customerName;
    private final String preferredSeatId;

    public CustomerBookingTask(TicketBookingEngine engine, String customerName, String preferredSeatId) {
        this.engine = engine;
        this.customerName = customerName;
        this.preferredSeatId = preferredSeatId;
    }

    @Override
    public void run() {
        // Random slight jitter before submission
        try {
            Thread.sleep(new Random().nextInt(40));
        } catch (InterruptedException ignored) {}

        engine.tryBookSeat(preferredSeatId, customerName);
    }
}
