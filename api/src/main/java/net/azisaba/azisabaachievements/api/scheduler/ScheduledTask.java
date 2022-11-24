package net.azisaba.azisabaachievements.api.scheduler;

public interface ScheduledTask {
    /**
     * Cancels the task if it has not been cancelled yet.
     */
    void cancel();
}
