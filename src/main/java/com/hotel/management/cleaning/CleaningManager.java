package com.hotel.management.cleaning;

import com.hotel.management.model.Room;
import javafx.scene.control.ProgressBar;

import java.util.LinkedList;
import java.util.Queue;

public class CleaningManager {

    public static final int MAX_CLEANERS = 3;

    private final Queue<PendingTask> taskQueue = new LinkedList<>();

    private int activeCleaners = 0;

    public CleaningManager() {
        for (int i = 1; i <= MAX_CLEANERS; i++) {
            Thread consumerThread = new Thread(this::consumerLoop, "Cleaner-Consumer-" + i);
            consumerThread.setDaemon(true);
            consumerThread.start();
            System.out.println("[CleaningManager] Started consumer thread: Cleaner-Consumer-" + i);
        }
    }

    public synchronized void submit(Room room, ProgressBar progressBar, Runnable onComplete) {
        taskQueue.add(new PendingTask(room, progressBar, onComplete));
        System.out.println("[CleaningManager] Task added for room " + room.getRoomNumber()
                + "  | Queue size: " + taskQueue.size());

        notifyAll();
    }

    private void consumerLoop() {
        while (true) {
            PendingTask task;

            synchronized (this) {
                while (taskQueue.isEmpty()) {
                    try {
                        System.out.println("[CleaningManager] "
                                + Thread.currentThread().getName() + " waiting (queue empty)...");
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                task = taskQueue.poll();
                activeCleaners++;
                System.out.println("[CleaningManager] "
                        + Thread.currentThread().getName()
                        + " picked room " + task.room.getRoomNumber()
                        + "  | Active=" + activeCleaners
                        + "  | Queue=" + taskQueue.size());
            }

            RoomCleaner cleaner = new RoomCleaner(
                    task.room,
                    task.progressBar,
                    () -> {
                        synchronized (this) {
                            activeCleaners--;
                            System.out.println("[CleaningManager] "
                                    + Thread.currentThread().getName()
                                    + " finished room " + task.room.getRoomNumber()
                                    + "  | Active=" + activeCleaners);
                        }
                        task.onComplete.run();
                    }
            );
            cleaner.run();
        }
    }

    public synchronized int getActiveCount() {
        return activeCleaners;
    }

    public synchronized int getQueueSize() {
        return taskQueue.size();
    }

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
