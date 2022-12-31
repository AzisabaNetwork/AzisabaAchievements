package net.azisaba.azisabaachievements.cli.scheduler

import net.azisaba.azisabaachievements.api.scheduler.ScheduledTask

object NoopScheduledTask : ScheduledTask {
    override fun cancel() {
        // Do nothing
    }
}
