package net.azisaba.azisabaachievements.cli.scheduler

import net.azisaba.azisabaachievements.api.scheduler.TaskBuilder
import net.azisaba.azisabaachievements.api.scheduler.TaskScheduler

object NoopTaskScheduler : TaskScheduler {
    override fun builder(runnable: Runnable): TaskBuilder = NoopTaskBuilder
}
