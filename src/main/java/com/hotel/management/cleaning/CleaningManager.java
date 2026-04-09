package com.hotel.management.cleaning;

import com.hotel.management.model.Room;
import javafx.scene.control.ProgressBar;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CleaningManager – controls how many rooms are cleaned in parallel.
 * <p>
 * Business rule: at most {@value #MAX_CLEANERS} cleaning threads at once.
 * Additional rooms are held in a FIFO waiting queue and started automatically
 * when a cleaner thread finishes.
 * </p>
 *
 * <p>Syllabus note: each cleaning task is given its own raw {@link Thread}
 * (not an ExecutorService) to demonstrate the Thread/Runnable pattern.</p>
 *
 * <p>Thread-safety: {@code activeCleaners} uses {@link AtomicInteger};
 * the queue is accessed only from the JavaFX Application Thread (all calls
 * come from FX event handlers or {@code Platform.runLater}) so no extra
 * lock is needed.</p>
 */
public class CleaningManager {

    /** Maximum number of rooms that can be cleaned simultaneously. */
    public static final int MAX_CLEANERS = 3;

    // ── State ────────────────────────────────────────────────────────────────
    private final AtomicInteger activeCleaners = new AtomicInteger(0);

    /** FIFO queue for rooms waiting to be cleaned. */
    private final Queue<PendingTask> waitingQueue = new LinkedList<>();

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Submit a room for cleaning.  If a cleaner slot is free the thread
     * starts immediately; otherwise the task is enqueued.
     *
     * @param room        Room whose status is already CLEANING.
     * @param progressBar The ProgressBar widget for this room's table row.
     * @param onComplete  FX-thread callback: update DB + UI when done.
     */
    public void submit(Room room, ProgressBar progressBar, Runnable onComplete) {
        if (activeCleaners.get() < MAX_CLEANERS) {
            startCleaning(room, progressBar, onComplete);
        } else {
            System.out.println("[CleaningManager] Queue: room " + room.getRoomNumber()
                    + " waiting (active=" + activeCleaners.get() + ")");
            waitingQueue.add(new PendingTask(room, progressBar, onComplete));
        }
    }

    /**
     * Returns the number of currently active cleaner threads.
     * Useful for displaying "Active cleaners: X / 3" in the UI.
     */
    public int getActiveCount() {
        return activeCleaners.get();
    }

    /**
     * Returns how many rooms are waiting in the queue.
     */
    public int getQueueSize() {
        return waitingQueue.size();
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private void startCleaning(Room room, ProgressBar progressBar, Runnable onComplete) {
        activeCleaners.incrementAndGet();
        int cleanerNumber = activeCleaners.get(); // label: Cleaner 1 / 2 / 3

        // Compose the completion callback: original onComplete + release slot
        Runnable wrappedOnComplete = () -> {
            onComplete.run();           // update DB + UI (runs on FX thread already)
            onCleanerFinished();        // release slot + maybe start queued task
        };

        RoomCleaner cleaner = new RoomCleaner(room, progressBar, wrappedOnComplete);
        Thread t = new Thread(cleaner, "Cleaner-" + cleanerNumber + "-Room-" + room.getRoomNumber());
        t.setDaemon(true);  // don't keep JVM alive after window closes
        t.start();

        System.out.println("[CleaningManager] Started thread " + t.getName()
                + " (active=" + activeCleaners.get() + ")");
    }

    /**
     * Called (on the FX thread, via Platform.runLater chain) when a cleaner
     * thread finishes.  Decrements the active count and starts the next
     * queued room if one exists.
     */
    private void onCleanerFinished() {
        activeCleaners.decrementAndGet();
        System.out.println("[CleaningManager] Cleaner finished. Active="
                + activeCleaners.get() + ", Queue=" + waitingQueue.size());

        PendingTask next = waitingQueue.poll();
        if (next != null) {
            System.out.println("[CleaningManager] Dequeuing room " + next.room.getRoomNumber());
            startCleaning(next.room, next.progressBar, next.onComplete);
        }
    }

    // ── Inner record for queued tasks ────────────────────────────────────────

    private static final class PendingTask {
        final Room        room;
        final ProgressBar progressBar;
        final Runnable    onComplete;

        PendingTask(Room room, ProgressBar progressBar, Runnable onComplete) {
            this.room        = room;
            this.progressBar = progressBar;
            this.onComplete  = onComplete;
        }
    }
}
