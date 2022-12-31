package net.azisaba.azisabaachievements.cli.scheduler

import net.azisaba.azisabaachievements.api.scheduler.ScheduledTask
import net.azisaba.azisabaachievements.api.scheduler.TaskBuilder
import java.util.concurrent.TimeUnit

object NoopTaskBuilder : TaskBuilder {
    override fun sync(): TaskBuilder = this

    override fun async(): TaskBuilder = this

    override fun delay(delay: Long, unit: TimeUnit): TaskBuilder = this

    override fun repeat(interval: Long, unit: TimeUnit): TaskBuilder = this

    override fun schedule(): ScheduledTask = NoopScheduledTask
}
