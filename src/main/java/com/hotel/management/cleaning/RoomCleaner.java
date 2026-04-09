package com.hotel.management.cleaning;

import com.hotel.management.model.Room;
import com.hotel.management.service.AuditManager;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

public class RoomCleaner implements Runnable {

    private final Room        room;
    private final ProgressBar progressBar;
    private final Runnable    onComplete;

    public RoomCleaner(Room room, ProgressBar progressBar, Runnable onComplete) {
        this.room        = room;
        this.progressBar = progressBar;
        this.onComplete  = onComplete;
    }

    @Override
    public void run() {
        int roomNum = 0;
        try {
            roomNum = Integer.parseInt(room.getRoomNumber());
        } catch (NumberFormatException ignored) {
        }

        System.out.println("[Cleaner] Starting room " + room.getRoomNumber()
                + " on thread " + Thread.currentThread().getName());

        AuditManager.getInstance().log("CLEANING_STARTED", roomNum, "N/A");

        try {
            for (int i = 1; i <= 100; i++) {
                Thread.sleep(300);

                final double progress = i / 100.0;
                Platform.runLater(() -> {
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                });
            }

            System.out.println("[Cleaner] Finished room " + room.getRoomNumber());

            AuditManager.getInstance().log("CLEANING_COMPLETED", roomNum, "N/A");

            Platform.runLater(onComplete);

        } catch (InterruptedException e) {
            System.out.println("[Cleaner] Interrupted while cleaning room "
                    + room.getRoomNumber());
            Thread.currentThread().interrupt();
        }
    }
}
