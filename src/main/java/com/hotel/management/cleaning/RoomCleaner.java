package com.hotel.management.cleaning;

import com.hotel.management.model.Room;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

/**
 * RoomCleaner implements Runnable.
 * <p>
 * Each instance cleans one hotel room in a dedicated thread.
 * Cleaning is simulated by sleeping 300 ms × 100 iterations ≈ 30 seconds.
 * Progress is pushed to the JavaFX thread via Platform.runLater so the
 * ProgressBar in the Cleaning tab updates smoothly.
 * </p>
 *
 * Syllabus note: uses Thread / Runnable pattern (not ExecutorService).
 */
public class RoomCleaner implements Runnable {

    private final Room        room;
    private final ProgressBar progressBar;
    private final Runnable    onComplete;   // called on the FX thread when done

    /**
     * @param room        The room being cleaned (its status is already CLEANING).
     * @param progressBar The ProgressBar widget in the Cleaning tab row.
     * @param onComplete  Callback executed (via Platform.runLater) when cleaning
     *                    finishes – the controller uses this to update the DB,
     *                    remove the row, and turn the room table row green.
     */
    public RoomCleaner(Room room, ProgressBar progressBar, Runnable onComplete) {
        this.room        = room;
        this.progressBar = progressBar;
        this.onComplete  = onComplete;
    }

    @Override
    public void run() {
        System.out.println("[Cleaner] Starting room " + room.getRoomNumber()
                + " on thread " + Thread.currentThread().getName());

        try {
            // 100 steps × 300 ms = 30 000 ms = 30 seconds
            for (int i = 1; i <= 100; i++) {
                Thread.sleep(300);          // simulate cleaning work

                final double progress = i / 100.0;
                Platform.runLater(() -> {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                });
            }

            System.out.println("[Cleaner] Finished room " + room.getRoomNumber());

            // Notify completion back on the FX thread
            Platform.runLater(onComplete);

        } catch (InterruptedException e) {
            System.out.println("[Cleaner] Interrupted while cleaning room "
                    + room.getRoomNumber());
            Thread.currentThread().interrupt();
        }
    }
}
