# Project 5: Multithreading — Ticket Booking System

## Overview
Demonstrates core Java Concurrency and Multi-Threaded synchronization mechanisms:
- **`ReentrantLock` & Fine-Grained Locking**: Each seat maintains its own fair `ReentrantLock` protecting the critical booking section without creating system-wide bottlenecks.
- **Race Condition Prevention**: Prevents two simultaneous customer threads from reserving the exact same seat concurrently (zero double-bookings).
- **`AtomicInteger`**: Lock-free atomic counters for metrics tracking (`totalSuccessfulBookings`, `raceConditionsPrevented`).
- **`ExecutorService` & Thread Pools**: Orchestrating fixed thread pools (`Executors.newFixedThreadPool(8)`) simulating high-traffic web reservations.
- **Thread Lifecycle & Latency Simulation**: Incorporating realistic processing delay (`Thread.sleep()`) and evaluating concurrent timing dynamics.

## Directory Layout
```
05_multithreading_ticket_booking/
├── src/main/java/com/learning/tickets/
│   ├── Seat.java
│   ├── TicketBookingEngine.java
│   ├── CustomerBookingTask.java
│   └── TicketBookingApplication.java
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── app.js
└── README.md
```

## Running the Java Application
```bash
javac -d bin src/main/java/com/learning/tickets/*.java
java -cp bin com.learning.tickets.TicketBookingApplication
```
