package com.learning.tickets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Driver demonstrating Java Multithreading with ExecutorService and Thread Pools.
 */
public class TicketBookingApplication {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("   JAVA MULTITHREADING: TICKET BOOKING SYSTEM     ");
        System.out.println("==================================================");

        // Initialize cinema with 4 VIP seats and 6 Regular seats (10 total seats)
        TicketBookingEngine engine = new TicketBookingEngine(4, 6);

        System.out.println("Initialized Inventory: 10 Total Seats (4 VIP, 6 Regular)");
        System.out.println("Simulating 16 concurrent customer threads competing for seats...\n");

        // Fixed thread pool simulating multiple concurrent web users
        ExecutorService executor = Executors.newFixedThreadPool(8);

        String[] customers = {
            "Alice", "Bob", "Charlie", "David",
            "Emma", "Frank", "Grace", "Hannah",
            "Ian", "Jack", "Karen", "Leo",
            "Mona", "Noah", "Olivia", "Paul"
        };

        // Notice intentionally colliding seat preferences to demonstrate race condition resolution!
        String[] preferredSeats = {
            "VIP-1", "VIP-1", // Alice and Bob fight for VIP-1
            "VIP-2", "VIP-2", // Charlie and David fight for VIP-2
            "VIP-3", "REG-1",
            "VIP-4", "REG-1", // Grace and Hannah fight for REG-1
            "REG-2", "REG-2", // Ian and Jack fight for REG-2
            "REG-3", "REG-4",
            "REG-5", "REG-6",
            "VIP-1", "REG-2"  // Olivia and Paul attempt already heavily contested seats
        };

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < customers.length; i++) {
            executor.submit(new CustomerBookingTask(engine, customers[i], preferredSeats[i]));
        }

        // Shutdown and wait for all concurrent tasks to finish
        executor.shutdown();
        boolean completed = executor.awaitTermination(5, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n==================================================");
        System.out.println("          CONCURRENCY EXECUTION REPORT            ");
        System.out.println("==================================================");
        System.out.printf("All Threads Finished:       %s (Elapsed: %d ms)\n", completed, duration);
        System.out.printf("Total Successful Bookings:  %d\n", engine.getSuccessfulBookingsCount());
        System.out.printf("Remaining Available Seats:  %d\n", engine.getAvailableCount());
        System.out.printf("Race Conflicts Prevented:   %d\n", engine.getRaceConditionsPrevented());
        System.out.println("--------------------------------------------------");
        System.out.println("Final Seating Chart Status:");
        for (Seat seat : engine.getAllSeats()) {
            System.out.println("  " + seat);
        }
        System.out.println("==================================================");
    }
}
